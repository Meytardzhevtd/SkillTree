import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllCourses, enrollToCourse } from '../services/courseApi';
import { getUser } from '../services/authStorage';

interface Course {
    courseId: number;
    title: string;
    description: string;
}

const CatalogPage: React.FC = () => {
    const [courses, setCourses] = useState<Course[]>([]);
    const [search, setSearch] = useState('');
    const [loading, setLoading] = useState<boolean>(true);
    const [searching, setSearching] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    const fetchCourses = useCallback(async (query: string) => {
        try {
            query === '' ? setLoading(true) : setSearching(true);
            const data = await getAllCourses(query);
            setCourses(data);
            setError(null);
        } catch (err) {
            console.error(err);
            setError('Не удалось загрузить список курсов');
        } finally {
            setLoading(false);
            setSearching(false);
        }
    }, []);

    useEffect(() => {
        fetchCourses('');
    }, [fetchCourses]);

    useEffect(() => {
        if (search === '') {
            fetchCourses('');
            return;
        }
        const timer = setTimeout(() => {
            fetchCourses(search);
        }, 350);
        return () => clearTimeout(timer);
    }, [search, fetchCourses]);

    const handleEnroll = async (courseId: number, courseName: string) => {
        const user = getUser();
        if (!user) {
            alert('Пользователь не авторизован');
            return;
        }
        try {
            await enrollToCourse(courseId, user.id, 'student');
            alert(`Вы успешно записались на курс "${courseName}"!`);
            navigate('/my-courses');
        } catch (err: any) {
            console.error(err);
            let message = 'Ошибка записи';
            if (err.response?.data) {
                message = typeof err.response.data === 'string'
                    ? err.response.data
                    : err.response.data.message || message;
            } else if (err.message) {
                message = err.message;
            }
            alert(message);
        }
    };

    if (loading) return <div style={{ padding: '20px' }}>Загрузка курсов...</div>;
    if (error) return <div style={{ padding: '20px', color: 'red' }}>{error}</div>;

    return (
        <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
            <h1>Каталог курсов</h1>

            <div style={{ position: 'relative', maxWidth: '480px', marginBottom: '28px' }}>
                <span style={{
                    position: 'absolute',
                    left: '12px',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    color: '#999',
                    fontSize: '16px',
                    pointerEvents: 'none',
                }}>
                    🔍
                </span>
                <input
                    type="text"
                    placeholder="Поиск курсов..."
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                    style={{
                        width: '100%',
                        padding: '10px 36px 10px 38px',
                        fontSize: '15px',
                        border: '1px solid #ced4da',
                        borderRadius: '8px',
                        outline: 'none',
                        boxSizing: 'border-box',
                        transition: 'border-color 0.2s',
                    }}
                    onFocus={e => (e.currentTarget.style.borderColor = '#007bff')}
                    onBlur={e => (e.currentTarget.style.borderColor = '#ced4da')}
                />
                {search && (
                    <button
                        onClick={() => setSearch('')}
                        style={{
                            position: 'absolute',
                            right: '10px',
                            top: '50%',
                            transform: 'translateY(-50%)',
                            background: 'none',
                            border: 'none',
                            cursor: 'pointer',
                            color: '#999',
                            fontSize: '16px',
                            padding: '0',
                            lineHeight: 1,
                        }}
                        title="Очистить"
                    >
                        ✕
                    </button>
                )}
            </div>

            {searching && (
                <p style={{ color: '#888', marginBottom: '16px' }}>Поиск...</p>
            )}

            {!searching && courses.length === 0 && (
                <p style={{ color: '#666' }}>
                    {search
                        ? `По запросу «${search}» курсов не найдено.`
                        : 'Пока нет доступных курсов.'}
                </p>
            )}

            {!searching && search && courses.length > 0 && (
                <p style={{ color: '#888', marginBottom: '16px', fontSize: '14px' }}>
                    Найдено курсов: {courses.length}
                </p>
            )}

            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '20px' }}>
                {courses.map(course => (
                    <div
                        key={course.courseId}
                        style={{
                            border: '1px solid #ddd',
                            borderRadius: '8px',
                            padding: '16px',
                            width: '280px',
                            transition: 'box-shadow 0.2s',
                            display: 'flex',
                            flexDirection: 'column',
                            justifyContent: 'space-between',
                        }}
                        onMouseEnter={e => (e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)')}
                        onMouseLeave={e => (e.currentTarget.style.boxShadow = 'none')}
                    >
                        <div>
                            <h2 style={{ margin: '0 0 8px 0', color: '#007bff', fontSize: '18px' }}>
                                {course.title}
                            </h2>
                            <p style={{ margin: '0 0 16px 0', color: '#666', fontSize: '14px', lineHeight: '1.5' }}>
                                {course.description || 'Нет описания'}
                            </p>
                        </div>
                        <button
                            onClick={() => handleEnroll(course.courseId, course.title)}
                            style={{
                                width: '100%',
                                padding: '8px 16px',
                                backgroundColor: '#007bff',
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                fontSize: '1rem',
                                transition: 'background-color 0.2s',
                            }}
                            onMouseEnter={e => (e.currentTarget.style.backgroundColor = '#0056b3')}
                            onMouseLeave={e => (e.currentTarget.style.backgroundColor = '#007bff')}
                        >
                            Выбрать
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default CatalogPage;

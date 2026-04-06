import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getAllCourses } from '../services/courseApi';

interface Course {
    courseId: number;
    name: string;
    description: string;
}

const CatalogPage: React.FC = () => {
    const [courses, setCourses] = useState<Course[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchCourses = async () => {
            try {
                const data = await getAllCourses();
                setCourses(data);
            } catch (err) {
                console.error(err);
                setError('Не удалось загрузить список курсов');
            } finally {
                setLoading(false);
            }
        };
        fetchCourses();
    }, []);

    const handleEnroll = (courseId: number, courseName: string) => {
        alert(`Функция записи на курс "${courseName}" (ID ${courseId}) будет доступна позже.`);
    };

    if (loading) return <div style={{ padding: '20px' }}>Загрузка курсов...</div>;
    if (error) return <div style={{ padding: '20px', color: 'red' }}>{error}</div>;

    return (
        <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
            {courses.length === 0 && <p>Пока нет доступных курсов.</p>}
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
                        }}
                    >
                        <Link
                            to={`/course/${course.courseId}`}
                            style={{ textDecoration: 'none', color: 'inherit' }}
                        >
                            <h2 style={{ margin: '0 0 8px 0', color: '#007bff' }}>{course.title}</h2>
                        </Link>
                        <p style={{ margin: '0 0 16px 0', color: '#666' }}>
                            {course.description || 'Нет описания'}
                        </p>
                        <button
                            onClick={() => handleEnroll(course.courseId, course.name)}
                            style={{
                                width: '100%',
                                padding: '8px 16px',
                                backgroundColor: '#007bff',
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                fontSize: '1rem',
                            }}
                            onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = '#0056b3')}
                            onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = '#007bff')}
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
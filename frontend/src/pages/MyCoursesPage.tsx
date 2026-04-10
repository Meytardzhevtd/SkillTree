import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyCoursesByRole, getMyTakenCourses } from '../services/courseApi';

interface CreatedCourse {
    courseId: number;
    title: string;
    description: string;
}

interface EnrolledCourse {
    takenCourseId: number;
    courseId: number;
    name: string;
    description: string;
    progress: number;
}

const MyCoursesPage: React.FC = () => {
    const [createdCourses, setCreatedCourses] = useState<CreatedCourse[]>([]);
    const [enrolledCourses, setEnrolledCourses] = useState<EnrolledCourse[]>([]);
    const [activeTab, setActiveTab] = useState<'created' | 'enrolled'>('created');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const loadCourses = async () => {
            try {
                const [created, enrolled] = await Promise.all([
                    getMyCoursesByRole('admin'), // ← только курсы где ты admin
                    getMyTakenCourses(),
                ]);
                setCreatedCourses(created);
                setEnrolledCourses(enrolled);
            } catch (err) {
                setError('Ошибка загрузки курсов');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };
        loadCourses();
    }, []);

    if (loading) return <p>Загрузка курсов...</p>;
    if (error) return <p style={{ color: 'red' }}>{error}</p>;

    const emptyMessage = activeTab === 'created'
        ? 'У вас пока нет созданных курсов'
        : 'Вы пока не записаны на курсы';

    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
            <h1>Мои курсы</h1>

            <div style={{ marginBottom: '20px', borderBottom: '1px solid #ddd', display: 'flex', gap: '16px' }}>
                <button
                    onClick={() => setActiveTab('created')}
                    style={{
                        padding: '8px 16px',
                        border: 'none',
                        background: activeTab === 'created' ? '#007bff' : 'transparent',
                        color: activeTab === 'created' ? 'white' : '#333',
                        cursor: 'pointer',
                        borderRadius: '4px 4px 0 0',
                    }}
                >
                    Мои курсы (созданные)
                </button>
                <button
                    onClick={() => setActiveTab('enrolled')}
                    style={{
                        padding: '8px 16px',
                        border: 'none',
                        background: activeTab === 'enrolled' ? '#007bff' : 'transparent',
                        color: activeTab === 'enrolled' ? 'white' : '#333',
                        cursor: 'pointer',
                        borderRadius: '4px 4px 0 0',
                    }}
                >
                    Курсы, которые я прохожу
                </button>
            </div>

            {activeTab === 'created' && createdCourses.length === 0 && <p>{emptyMessage}</p>}
            {activeTab === 'enrolled' && enrolledCourses.length === 0 && <p>{emptyMessage}</p>}

            {activeTab === 'created' && createdCourses.length > 0 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    {createdCourses.map((course) => (
                        <div
                            key={course.courseId}
                            style={{ border: '1px solid #ddd', borderRadius: '8px', padding: '16px', transition: 'box-shadow 0.2s' }}
                            onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)'}
                            onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'none'}
                        >
                            <Link to={`/course/${course.courseId}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                                <h2 style={{ margin: '0 0 8px 0', color: '#007bff' }}>{course.title}</h2>
                                <p style={{ margin: 0, color: '#666' }}>{course.description}</p>
                            </Link>
                        </div>
                    ))}
                </div>
            )}

            {activeTab === 'enrolled' && enrolledCourses.length > 0 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    {enrolledCourses.map((course) => (
                        <div
                            key={course.courseId}
                            style={{ border: '1px solid #ddd', borderRadius: '8px', padding: '16px', transition: 'box-shadow 0.2s' }}
                            onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)'}
                            onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'none'}
                        >
                            <Link
                                to={`/course/${course.courseId}?takenCourseId=${course.takenCourseId}`}
                                style={{ textDecoration: 'none', color: 'inherit' }}
                            >
                                <h2 style={{ margin: '0 0 8px 0', color: '#007bff' }}>{course.name}</h2>
                                <p style={{ margin: 0, color: '#666' }}>{course.description}</p>
                                <div style={{ marginTop: '10px' }}>
                                    <div style={{ background: '#e9ecef', borderRadius: '4px', height: '6px', overflow: 'hidden' }}>
                                        <div style={{
                                            background: '#28a745',
                                            height: '100%',
                                            width: `${course.progress}%`,
                                            transition: 'width 0.3s'
                                        }} />
                                    </div>
                                    <p style={{ margin: '4px 0 0 0', fontSize: '0.8rem', color: '#888' }}>
                                        Прогресс: {course.progress.toFixed(0)}%
                                    </p>
                                </div>
                            </Link>
                        </div>
                    ))}
                </div>
            )}

            {activeTab === 'created' && (
                <div style={{ marginTop: '24px' }}>
                    <Link to="/create-course">
                        <button>➕ Создать новый курс</button>
                    </Link>
                </div>
            )}
        </div>
    );
};

export default MyCoursesPage;

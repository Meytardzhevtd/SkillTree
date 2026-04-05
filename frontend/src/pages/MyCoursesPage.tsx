// src/pages/MyCoursesPage.tsx
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyCreatedCourses, getMyEnrolledCourses } from '../services/courseApi';

interface Course {
    courseId: number;
    title: string;
    description: string;
}

const MyCoursesPage: React.FC = () => {
    const [createdCourses, setCreatedCourses] = useState<Course[]>([]);
    const [enrolledCourses, setEnrolledCourses] = useState<Course[]>([]);
    const [activeTab, setActiveTab] = useState<'created' | 'enrolled'>('created');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const loadCourses = async () => {
            try {
                const [created, enrolled] = await Promise.all([
                    getMyCreatedCourses(),
                    getMyEnrolledCourses(),
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

    const courses = activeTab === 'created' ? createdCourses : enrolledCourses;
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

            {courses.length === 0 ? (
                <p>{emptyMessage}</p>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    {courses.map((course) => (
                        <div
                            key={course.courseId}
                            style={{
                                border: '1px solid #ddd',
                                borderRadius: '8px',
                                padding: '16px',
                                transition: 'box-shadow 0.2s',
                            }}
                        >
                            <Link
                                to={`/course/${course.courseId}`}
                                style={{ textDecoration: 'none', color: 'inherit' }}
                            >
                                <h2 style={{ margin: '0 0 8px 0', color: '#007bff' }}>{course.title}</h2>
                                <p style={{ margin: 0, color: '#666' }}>{course.description}</p>
                            </Link>
                        </div>
                    ))}
                </div>
            )}

            <div style={{ marginTop: '24px' }}>
                <Link to="/create-course">
                    <button>➕ Создать новый курс</button>
                </Link>
            </div>
        </div>
    );
};

export default MyCoursesPage;
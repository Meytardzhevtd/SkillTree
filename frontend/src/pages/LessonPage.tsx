import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import {
    getLessonById,
    getLessonsByModuleId,
    getTasksByModuleId,
    getMyRoleInCourse,
    deleteLesson,
} from '../services/courseApi';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeHighlight from 'rehype-highlight';
import 'highlight.js/styles/github-dark.css';


interface Lesson {
    id: number;
    title: string;
    content: string;
    moduleId: number;
}

const LessonPage: React.FC = () => {
    const { lessonId } = useParams<{ lessonId: string }>();
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const [lesson, setLesson] = useState<Lesson | null>(null);
    const [allLessons, setAllLessons] = useState<Lesson[]>([]);
    const [firstTaskId, setFirstTaskId] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [isAdmin, setIsAdmin] = useState(false);

    useEffect(() => {
        loadLesson();
    }, [lessonId]);

    const loadLesson = async () => {
        try {
            setLoading(true);

            const lessonData: Lesson = await getLessonById(Number(lessonId));
            setLesson(lessonData);

            // Берём courseId из URL (он передаётся из модуля)
            const courseId = searchParams.get('courseId');
            if (courseId) {
                const role = await getMyRoleInCourse(Number(courseId));
                setIsAdmin(role === 'admin');
            }

            const [lessonsData, tasksData] = await Promise.all([
                getLessonsByModuleId(lessonData.moduleId),
                getTasksByModuleId(lessonData.moduleId),
            ]);

            setAllLessons(lessonsData);
            setFirstTaskId(tasksData.length > 0 ? tasksData[0].id : null);

        } catch (err) {
            console.error('Ошибка загрузки урока:', err);
        } finally {
            setLoading(false);
        }
    };

    const buildQuery = () => {
        const takenCourseId = searchParams.get('takenCourseId');
        const courseId = searchParams.get('courseId');
        const params = new URLSearchParams();
        if (takenCourseId) params.set('takenCourseId', takenCourseId);
        if (courseId) params.set('courseId', courseId);
        return params.toString() ? `?${params.toString()}` : '';
    };

    const currentIdx = allLessons.findIndex(l => l.id === Number(lessonId));
    const isFirst = currentIdx === 0;
    const isLast = currentIdx === allLessons.length - 1;

    const handlePrev = () => {
        if (!isFirst && allLessons[currentIdx - 1]) {
            navigate(`/lesson/${allLessons[currentIdx - 1].id}${buildQuery()}`);
        }
    };

    const handleNext = () => {
        if (!isLast && allLessons[currentIdx + 1]) {
            navigate(`/lesson/${allLessons[currentIdx + 1].id}${buildQuery()}`);
        }
    };

    const handleGoToTasks = () => {
        if (firstTaskId !== null) {
            navigate(`/task/${firstTaskId}${buildQuery()}`);
        }
    };

    const handleDelete = async () => {
        if (!confirm('Удалить урок?')) return;
        try {
            await deleteLesson(Number(lessonId));
            navigate(-1);
        } catch (err) {
            console.error('Ошибка удаления:', err);
            alert('Ошибка при удалении урока');
        }
    };

    if (loading) return <div style={{ padding: '20px' }}>Загрузка урока...</div>;
    if (!lesson) return <div style={{ padding: '20px' }}>Урок не найден</div>;

    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <button onClick={() => navigate(-1)}>← Назад к модулю</button>
                <span style={{ color: '#888', fontSize: '14px' }}>
                    Урок {currentIdx + 1} из {allLessons.length}
                </span>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <button onClick={handlePrev} disabled={isFirst}>‹ Пред.</button>
                    <button onClick={handleNext} disabled={isLast}>След. ›</button>
                </div>
            </div>

            <div style={{ border: '1px solid #ddd', borderRadius: '8px', padding: '24px', background: '#fff' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                    <div>
                        <span style={{
                            fontSize: '12px',
                            background: '#e9ecef',
                            padding: '2px 8px',
                            borderRadius: '4px',
                            display: 'inline-block',
                            marginBottom: '8px',
                        }}>
                            Теория
                        </span>
                        <h2 style={{ margin: 0, fontSize: '18px', color: '#213547' }}>
                            {lesson.title}
                        </h2>
                    </div>
                    {isAdmin && (
                        <button
                            onClick={handleDelete}
                            style={{
                                background: '#dc3545',
                                color: 'white',
                                border: 'none',
                                padding: '6px 12px',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                flexShrink: 0,
                            }}
                        >
                            Удалить урок
                        </button>
                    )}
                </div>

                <ReactMarkdown
                    remarkPlugins={[remarkGfm]}
                    rehypePlugins={[rehypeHighlight]}
                    components={{
                        table: ({ children }) => (
                            <div style={{ overflowX: 'auto' }}>
                                <table style={{ borderCollapse: 'collapse', width: '100%' }}>
                                    {children}
                                </table>
                            </div>
                        ),
                        th: ({ children }) => (
                            <th style={{ border: '1px solid #ddd', padding: '8px', background: '#f5f5f5' }}>
                                {children}
                            </th>
                        ),
                        td: ({ children }) => (
                            <td style={{ border: '1px solid #ddd', padding: '8px' }}>
                                {children}
                            </td>
                        ),
                        code: ({ className, children }) => {
                            const match = /language-(\w+)/.exec(className || '');
                            return match ? (
                                <code style={{ background: '#f5f5f5', padding: '2px 4px', borderRadius: '4px' }}>
                                    {children}
                                </code>
                            ) : (
                                <code style={{ background: '#f5f5f5', padding: '2px 4px', borderRadius: '4px' }}>
                                    {children}
                                </code>
                            );
                        }
                    }}
                >
                    {lesson.content}
                </ReactMarkdown>

                <div style={{ display: 'flex', gap: '8px', marginTop: '24px' }}>
                    {!isLast && (
                        <button
                            onClick={handleNext}
                            style={{
                                padding: '8px 16px',
                                background: '#007bff',
                                color: 'white',
                                border: 'none',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontSize: '14px',
                            }}
                        >
                            Следующий урок →
                        </button>
                    )}
                    {isLast && firstTaskId !== null && (
                        <button
                            onClick={handleGoToTasks}
                            style={{
                                padding: '8px 16px',
                                background: '#28a745',
                                color: 'white',
                                border: 'none',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontSize: '14px',
                            }}
                        >
                            Перейти к задачам →
                        </button>
                    )}
                    {isLast && firstTaskId === null && (
                        <button
                            onClick={() => navigate(-1)}
                            style={{
                                padding: '8px 16px',
                                background: '#28a745',
                                color: 'white',
                                border: 'none',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontSize: '14px',
                            }}
                        >
                            Завершить модуль ✓
                        </button>
                    )}
                </div>
            </div>

            <div style={{ marginTop: '24px' }}>
                <h3 style={{ marginBottom: '12px' }}>Уроки модуля</h3>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                    {allLessons.map((l, idx) => {
                        const isCurrent = l.id === Number(lessonId);
                        return (
                            <button
                                key={l.id}
                                onClick={() => navigate(`/lesson/${l.id}${buildQuery()}`)}
                                style={{
                                    padding: '8px 14px',
                                    borderRadius: '6px',
                                    border: isCurrent ? '2px solid #007bff' : '1px solid #ddd',
                                    background: isCurrent ? '#e8f0fe' : '#f9f9f9',
                                    color: '#213547',
                                    fontWeight: isCurrent ? 'bold' : 'normal',
                                    cursor: 'pointer',
                                }}
                            >
                                {idx + 1}
                            </button>
                        );
                    })}
                </div>
            </div>
        </div>
    );
};

export default LessonPage;
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { getTaskById, getTasksByModuleId, startModule, submitAnswer, getMyTakenCourses } from '../services/courseApi';

interface TaskContent {
    type: 'ONE_POSSIBLE_ANSWER' | 'MULTIPLE';
    question: string;
    options: string[];
    indexCorrectAnswer?: number;
    correctAnswers?: number[];
}

interface Task {
    id: number;
    taskTypeId: number;
    moduleId: number;
    content: TaskContent;
}

interface SubmitResult {
    correct: boolean;
    alreadySolved: boolean;
    message: string;
    moduleProgress: number;
    tasks: { taskId: number; isCompleted: boolean }[];
}

const TaskPage: React.FC = () => {
    const { taskId } = useParams<{ taskId: string }>();
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const [task, setTask] = useState<Task | null>(null);
    const [allTasks, setAllTasks] = useState<Task[]>([]);
    const [progressModuleId, setProgressModuleId] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // Состояние ответа
    const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
    const [selectedIndexes, setSelectedIndexes] = useState<number[]>([]);

    // Состояние результата
    const [result, setResult] = useState<SubmitResult | null>(null);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        loadTask();
    }, [taskId]);

    const loadTask = async () => {
        try {
            setLoading(true);
            setError('');
            setResult(null);
            setSelectedIndex(null);
            setSelectedIndexes([]);

            // 1. Загружаем таску
            const taskData = await getTaskById(Number(taskId));
            setTask(taskData);

            // 2. Загружаем все таски модуля для навигации
            const tasksData = await getTasksByModuleId(taskData.moduleId);
            setAllTasks(tasksData);

            // 3. Получаем takenCourseId — из query params или из списка курсов пользователя
            let takenCourseId = searchParams.get('takenCourseId')
                ? Number(searchParams.get('takenCourseId'))
                : null;

            if (!takenCourseId) {
                // Ищем takenCourse по courseId модуля
                const takenCourses = await getMyTakenCourses();
                // getTaskById не возвращает courseId напрямую, поэтому берём первый подходящий
                // В идеале ModulePage должен передавать takenCourseId через query param
                const found = takenCourses.find((tc: any) => tc.courseId != null);
                takenCourseId = found?.takenCourseId ?? null;
            }

            if (!takenCourseId) {
                setError('Не удалось определить прохождение курса. Вернитесь на страницу модуля.');
                return;
            }

            // 4. Стартуем модуль — получаем progressModuleId
            const startResult = await startModule(taskData.moduleId, takenCourseId);
            setProgressModuleId(startResult.progressModuleId);

        } catch (err) {
            console.error('Ошибка загрузки задачи:', err);
            setError('Ошибка загрузки задачи');
        } finally {
            setLoading(false);
        }
    };

    const handleToggleMultiple = (index: number) => {
        setSelectedIndexes(prev =>
            prev.includes(index)
                ? prev.filter(i => i !== index)
                : [...prev, index]
        );
    };

    const handleSubmit = async () => {
        if (!task || progressModuleId === null) return;

        if (task.content.type === 'ONE_POSSIBLE_ANSWER' && selectedIndex === null) {
            alert('Выберите вариант ответа');
            return;
        }
        if (task.content.type === 'MULTIPLE' && selectedIndexes.length === 0) {
            alert('Выберите хотя бы один вариант ответа');
            return;
        }

        try {
            setSubmitting(true);
            const answer = task.content.type === 'ONE_POSSIBLE_ANSWER'
                ? selectedIndex!
                : selectedIndexes;

            const res = await submitAnswer(Number(taskId), progressModuleId, answer);
            setResult(res);
        } catch (err) {
            console.error('Ошибка отправки ответа:', err);
            alert('Ошибка при отправке ответа');
        } finally {
            setSubmitting(false);
        }
    };

    const getCurrentTaskIndex = () => allTasks.findIndex(t => t.id === Number(taskId));

    const handleNavigate = (direction: 'prev' | 'next') => {
        const idx = getCurrentTaskIndex();
        const nextIdx = direction === 'prev' ? idx - 1 : idx + 1;
        if (nextIdx >= 0 && nextIdx < allTasks.length) {
            const takenCourseId = searchParams.get('takenCourseId');
            const query = takenCourseId ? `?takenCourseId=${takenCourseId}` : '';
            navigate(`/task/${allTasks[nextIdx].id}${query}`);
        }
    };

    if (loading) {
        return <div style={{ padding: '20px' }}>Загрузка задачи...</div>;
    }

    if (error) {
        return (
            <div style={{ padding: '20px' }}>
                <p style={{ color: 'red' }}>{error}</p>
                <button onClick={() => navigate(-1)}>← Назад</button>
            </div>
        );
    }

    if (!task) return null;

    const currentIdx = getCurrentTaskIndex();
    const isCompleted = result?.tasks.find(t => t.taskId === Number(taskId))?.isCompleted ?? false;

    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>

            {/* Навигация */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <button onClick={() => navigate(-1)}>← Назад к модулю</button>
                <span style={{ color: '#888', fontSize: '14px' }}>
                    Задача {currentIdx + 1} из {allTasks.length}
                </span>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                        onClick={() => handleNavigate('prev')}
                        disabled={currentIdx === 0}
                    >
                        ‹ Пред.
                    </button>
                    <button
                        onClick={() => handleNavigate('next')}
                        disabled={currentIdx === allTasks.length - 1}
                    >
                        След. ›
                    </button>
                </div>
            </div>

            {/* Карточка задачи */}
            <div style={{
                border: '1px solid #ddd',
                borderRadius: '8px',
                padding: '24px',
                background: '#fff'
            }}>
                {/* Тип задачи */}
                <span style={{
                    fontSize: '12px',
                    background: '#e9ecef',
                    padding: '2px 8px',
                    borderRadius: '4px',
                    marginBottom: '12px',
                    display: 'inline-block'
                }}>
                    {task.content.type === 'ONE_POSSIBLE_ANSWER' ? 'Один правильный ответ' : 'Несколько правильных ответов'}
                </span>

                {/* Вопрос */}
                <h2 style={{ margin: '12px 0 20px 0', fontSize: '18px', color: '#213547' }}>
                    {task.content.question}
                </h2>

                {/* Варианты ответов */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                    {task.content.options.map((option, idx) => {
                        const isSelected = task.content.type === 'ONE_POSSIBLE_ANSWER'
                            ? selectedIndex === idx
                            : selectedIndexes.includes(idx);

                        // Подсветка после ответа
                        let borderColor = '#ddd';
                        let background = '#fff';
                        if (result) {
                            const correct = task.content.type === 'ONE_POSSIBLE_ANSWER'
                                ? idx === task.content.indexCorrectAnswer
                                : task.content.correctAnswers?.includes(idx);
                            if (correct) {
                                borderColor = '#28a745';
                                background = '#d4edda';
                            } else if (isSelected && !correct) {
                                borderColor = '#dc3545';
                                background = '#f8d7da';
                            }
                        } else if (isSelected) {
                            borderColor = '#007bff';
                            background = '#e8f0fe';
                        }

                        return (
                            <div
                                key={idx}
                                onClick={() => {
                                    if (result) return; // после ответа не менять
                                    if (task.content.type === 'ONE_POSSIBLE_ANSWER') {
                                        setSelectedIndex(idx);
                                    } else {
                                        handleToggleMultiple(idx);
                                    }
                                }}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '12px',
                                    padding: '12px 16px',
                                    border: `2px solid ${borderColor}`,
                                    borderRadius: '8px',
                                    cursor: result ? 'default' : 'pointer',
                                    background,
                                    transition: 'border-color 0.2s, background 0.2s',
                                }}
                            >
                                <input
                                    type={task.content.type === 'ONE_POSSIBLE_ANSWER' ? 'radio' : 'checkbox'}
                                    checked={isSelected}
                                    onChange={() => {}}
                                    style={{ pointerEvents: 'none' }}
                                />
                                <span style={{ color: '#213547' }}>{option}</span>
                            </div>
                        );
                    })}
                </div>

                {/* Кнопка ответить */}
                {!result && (
                    <button
                        onClick={handleSubmit}
                        disabled={submitting || (task.content.type === 'ONE_POSSIBLE_ANSWER' ? selectedIndex === null : selectedIndexes.length === 0)}
                        style={{
                            marginTop: '20px',
                            padding: '10px 24px',
                            background: '#007bff',
                            color: 'white',
                            border: 'none',
                            borderRadius: '6px',
                            fontSize: '15px',
                            opacity: submitting ? 0.7 : 1,
                        }}
                    >
                        {submitting ? 'Проверка...' : 'Ответить'}
                    </button>
                )}

                {/* Результат */}
                {result && (
                    <div style={{
                        marginTop: '20px',
                        padding: '16px',
                        borderRadius: '8px',
                        background: result.correct ? '#d4edda' : '#f8d7da',
                        border: `1px solid ${result.correct ? '#28a745' : '#dc3545'}`,
                    }}>
                        <strong style={{ color: result.correct ? '#155724' : '#721c24', fontSize: '16px' }}>
                            {result.correct ? '✓ ' : '✗ '}{result.message}
                        </strong>
                        <p style={{ margin: '8px 0 0 0', color: '#555', fontSize: '14px' }}>
                            Прогресс модуля: {result.moduleProgress.toFixed(0)}%
                        </p>

                        {/* Кнопки после ответа */}
                        <div style={{ display: 'flex', gap: '8px', marginTop: '12px' }}>
                            {!result.correct && (
                                <button
                                    onClick={() => {
                                        setResult(null);
                                        setSelectedIndex(null);
                                        setSelectedIndexes([]);
                                    }}
                                    style={{
                                        padding: '8px 16px',
                                        background: '#dc3545',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '6px',
                                    }}
                                >
                                    Попробовать ещё раз
                                </button>
                            )}
                            {currentIdx < allTasks.length - 1 && (
                                <button
                                    onClick={() => handleNavigate('next')}
                                    style={{
                                        padding: '8px 16px',
                                        background: '#28a745',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '6px',
                                    }}
                                >
                                    Следующая задача →
                                </button>
                            )}
                            {currentIdx === allTasks.length - 1 && (
                                <button
                                    onClick={() => navigate(-1)}
                                    style={{
                                        padding: '8px 16px',
                                        background: '#28a745',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '6px',
                                    }}
                                >
                                    Завершить модуль ✓
                                </button>
                            )}
                        </div>
                    </div>
                )}
            </div>

            {/* Список всех задач модуля */}
            {result && (
                <div style={{ marginTop: '24px' }}>
                    <h3 style={{ marginBottom: '12px' }}>Задачи модуля</h3>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                        {result.tasks.map((t, idx) => {
                            const isCurrent = t.taskId === Number(taskId);
                            const takenCourseId = searchParams.get('takenCourseId');
                            const query = takenCourseId ? `?takenCourseId=${takenCourseId}` : '';
                            return (
                                <button
                                    key={t.taskId}
                                    onClick={() => navigate(`/task/${t.taskId}${query}`)}
                                    style={{
                                        padding: '8px 14px',
                                        borderRadius: '6px',
                                        border: isCurrent ? '2px solid #007bff' : '1px solid #ddd',
                                        background: t.isCompleted ? '#28a745' : '#f9f9f9',
                                        color: t.isCompleted ? 'white' : '#213547',
                                        fontWeight: isCurrent ? 'bold' : 'normal',
                                    }}
                                >
                                    {idx + 1}
                                </button>
                            );
                        })}
                    </div>
                </div>
            )}
        </div>
    );
};

export default TaskPage;

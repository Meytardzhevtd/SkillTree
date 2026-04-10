import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { getTasksByModuleId, createTask, getMyRoleInCourse } from '../services/courseApi';

interface Task {
    id: number;
    taskTypeId: number;
    moduleId: number;
    content: {
        type: string;
        question: string;
        options?: string[];
        indexCorrectAnswer?: number;
        correctAnswers?: number[];
    };
}

const ModulePage: React.FC = () => {
    const { moduleId } = useParams<{ moduleId: string }>();
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const [tasks, setTasks] = useState<Task[]>([]);
    const [loading, setLoading] = useState(true);
    const [isAdmin, setIsAdmin] = useState(false);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [creating, setCreating] = useState(false);

    const [taskType, setTaskType] = useState<'ONE_POSSIBLE_ANSWER' | 'MULTIPLE'>('ONE_POSSIBLE_ANSWER');
    const [question, setQuestion] = useState('');
    const [options, setOptions] = useState<string[]>(['', '']);
    const [correctIndex, setCorrectIndex] = useState(0);
    const [correctAnswers, setCorrectAnswers] = useState<number[]>([]);

    useEffect(() => {
        loadAll();
    }, [moduleId]);

    const loadAll = async () => {
        try {
            setLoading(true);

            const tasksData = await getTasksByModuleId(Number(moduleId));
            setTasks(tasksData);

            const courseId = searchParams.get('courseId');
            if (courseId) {
                const role = await getMyRoleInCourse(Number(courseId));
                setIsAdmin(role === 'admin');
            }
        } catch (err) {
            console.error('Ошибка загрузки:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleAddOption = () => {
        setOptions([...options, '']);
    };

    const handleRemoveOption = (index: number) => {
        const newOptions = options.filter((_, i) => i !== index);
        setOptions(newOptions);
        if (taskType === 'ONE_POSSIBLE_ANSWER') {
            if (correctIndex >= newOptions.length) setCorrectIndex(0);
        } else {
            const newCorrectAnswers = correctAnswers
                .filter(i => i !== index)
                .map(i => i > index ? i - 1 : i);
            setCorrectAnswers(newCorrectAnswers);
        }
    };

    const handleOptionChange = (index: number, value: string) => {
        const newOptions = [...options];
        newOptions[index] = value;
        setOptions(newOptions);
    };

    const handleCorrectAnswerToggle = (index: number) => {
        if (taskType === 'ONE_POSSIBLE_ANSWER') {
            setCorrectIndex(index);
        } else {
            if (correctAnswers.includes(index)) {
                setCorrectAnswers(correctAnswers.filter(i => i !== index));
            } else {
                setCorrectAnswers([...correctAnswers, index]);
            }
        }
    };

    const handleCreateTask = async () => {
        if (!question.trim()) {
            alert('Введите вопрос');
            return;
        }
        const filteredOptions = options.filter(opt => opt.trim() !== '');
        if (filteredOptions.length < 2) {
            alert('Добавьте минимум 2 варианта ответа');
            return;
        }

        const content: any = {
            type: taskType,
            question,
            options: filteredOptions,
        };

        if (taskType === 'ONE_POSSIBLE_ANSWER') {
            content.indexCorrectAnswer = correctIndex;
        } else {
            if (correctAnswers.length === 0) {
                alert('Выберите хотя бы один правильный ответ');
                return;
            }
            content.correctAnswers = correctAnswers;
        }

        try {
            setCreating(true);
            await createTask(Number(moduleId), taskType === 'ONE_POSSIBLE_ANSWER' ? 1 : 2, content);
            setQuestion('');
            setOptions(['', '']);
            setCorrectIndex(0);
            setCorrectAnswers([]);
            setShowCreateForm(false);
            await loadAll();
        } catch (err) {
            console.error('Ошибка создания задачи:', err);
            alert('Ошибка при создании задачи');
        } finally {
            setCreating(false);
        }
    };

    if (loading) {
        return <div style={{ padding: '20px' }}>Загрузка задач...</div>;
    }

    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
            <button onClick={() => navigate(-1)} style={{ marginBottom: '16px' }}>
                ← Назад
            </button>

            <h1>Модуль #{moduleId}</h1>

            {isAdmin && (
                <button
                    onClick={() => setShowCreateForm(!showCreateForm)}
                    style={{ marginBottom: '20px', padding: '8px 16px' }}
                >
                    {showCreateForm ? '❌ Отмена' : '+ Создать задачу'}
                </button>
            )}

            {isAdmin && showCreateForm && (
                <div style={{
                    border: '1px solid #007bff',
                    borderRadius: '8px',
                    padding: '20px',
                    marginBottom: '20px',
                    background: '#f8f9fa'
                }}>
                    <h3>Новая задача</h3>

                    <div style={{ marginBottom: '16px' }}>
                        <label>Тип задачи:</label>
                        <select
                            value={taskType}
                            onChange={(e) => setTaskType(e.target.value as any)}
                            style={{ marginLeft: '8px', padding: '4px' }}
                        >
                            <option value="ONE_POSSIBLE_ANSWER">Один правильный ответ</option>
                            <option value="MULTIPLE">Несколько правильных ответов</option>
                        </select>
                    </div>

                    <div style={{ marginBottom: '16px' }}>
                        <label>Вопрос:</label>
                        <textarea
                            value={question}
                            onChange={(e) => setQuestion(e.target.value)}
                            placeholder="Введите вопрос..."
                            style={{ width: '100%', padding: '8px', marginTop: '4px', minHeight: '60px' }}
                        />
                    </div>

                    <div style={{ marginBottom: '16px' }}>
                        <label>Варианты ответов:</label>
                        {options.map((opt, idx) => (
                            <div key={idx} style={{ display: 'flex', alignItems: 'center', marginTop: '8px', gap: '8px' }}>
                                <input
                                    type={taskType === 'ONE_POSSIBLE_ANSWER' ? 'radio' : 'checkbox'}
                                    name="correct"
                                    checked={taskType === 'ONE_POSSIBLE_ANSWER'
                                        ? correctIndex === idx
                                        : correctAnswers.includes(idx)}
                                    onChange={() => handleCorrectAnswerToggle(idx)}
                                />
                                <input
                                    type="text"
                                    value={opt}
                                    onChange={(e) => handleOptionChange(idx, e.target.value)}
                                    placeholder={`Вариант ${idx + 1}`}
                                    style={{ flex: 1, padding: '6px' }}
                                />
                                <button onClick={() => handleRemoveOption(idx)} disabled={options.length <= 2}>
                                    ✖
                                </button>
                            </div>
                        ))}
                        <button onClick={handleAddOption} style={{ marginTop: '8px' }}>
                            + Добавить вариант
                        </button>
                    </div>

                    <button
                        onClick={handleCreateTask}
                        disabled={creating}
                        style={{ padding: '8px 16px', background: '#28a745', color: 'white', border: 'none', borderRadius: '4px' }}
                    >
                        {creating ? 'Создание...' : 'Создать задачу'}
                    </button>
                </div>
            )}

            <h2>Задачи модуля</h2>

            {tasks.length === 0 ? (
                <p>
                    {isAdmin ? 'В модуле пока нет задач. Создайте первую задачу!' : 'В модуле пока нет задач.'}
                </p>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {tasks.map((task) => (
                        <div
                            key={task.id}
                            style={{
                                border: '1px solid #ddd',
                                borderRadius: '8px',
                                padding: '16px',
                                cursor: 'pointer',
                                transition: 'box-shadow 0.2s',
                            }}
                            onClick={() => navigate(`/task/${task.id}`)}
                            onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)'}
                            onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'none'}
                        >
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div>
                                    <strong>Задача #{task.id}</strong>
                                    <span style={{
                                        marginLeft: '8px',
                                        fontSize: '12px',
                                        background: '#e9ecef',
                                        padding: '2px 6px',
                                        borderRadius: '4px'
                                    }}>
                                        {task.content.type === 'ONE_POSSIBLE_ANSWER' ? 'Один ответ' : 'Множественный выбор'}
                                    </span>
                                    <p style={{ margin: '8px 0 0 0', color: '#333' }}>{task.content.question}</p>
                                </div>
                                <span>→</span>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default ModulePage;

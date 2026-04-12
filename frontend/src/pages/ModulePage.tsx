import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useSearchParams, useLocation } from 'react-router-dom';
import { getTasksByModuleId, createTask, getMyRoleInCourse, startModule } from '../services/courseApi';

interface Task {
    id: number;
    taskTypeId: number;
    moduleId: number;
    isCompleted: boolean;
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
    const location = useLocation();

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
    }, [moduleId, location.key]);

    const loadAll = async () => {
        try {
            setLoading(true);

            const courseId = searchParams.get('courseId');
            const takenCourseId = searchParams.get('takenCourseId');

            let role = 'none';
            if (courseId) {
                role = await getMyRoleInCourse(Number(courseId));
                setIsAdmin(role === 'admin');
            }

            let pmId: number | null = null;
            if (role === 'student' && takenCourseId) {
                const startResult = await startModule(Number(moduleId), Number(takenCourseId));
                pmId = startResult.progressModuleId;
            }

            const tasksData = await getTasksByModuleId(
                Number(moduleId),
                pmId ?? undefined
            );
            setTasks(tasksData);

        } catch (err) {
            console.error('Ошибка загрузки:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleBack = () => {
        const courseId = searchParams.get('courseId');
        const takenCourseId = searchParams.get('takenCourseId');
        if (courseId) {
            const params = new URLSearchParams();
            if (takenCourseId) params.set('takenCourseId', takenCourseId);
            navigate(`/course/${courseId}${params.toString() ? `?${params.toString()}` : ''}`);
        } else {
            navigate(-1);
        }
    };

    const handleAddOption = () => setOptions([...options, '']);

    const handleRemoveOption = (index: number) => {
        const newOptions = options.filter((_, i) => i !== index);
        setOptions(newOptions);
        if (taskType === 'ONE_POSSIBLE_ANSWER') {
            if (correctIndex >= newOptions.length) setCorrectIndex(0);
        } else {
            setCorrectAnswers(correctAnswers.filter(i => i !== index).map(i => i > index ? i - 1 : i));
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
            setCorrectAnswers(prev =>
                prev.includes(index) ? prev.filter(i => i !== index) : [...prev, index]
            );
        }
    };

    const handleCreateTask = async () => {
        if (!question.trim()) { alert('Введите вопрос'); return; }
        const filteredOptions = options.filter(opt => opt.trim() !== '');
        if (filteredOptions.length < 2) { alert('Добавьте минимум 2 варианта ответа'); return; }

        const content: any = { type: taskType, question, options: filteredOptions };
        if (taskType === 'ONE_POSSIBLE_ANSWER') {
            content.indexCorrectAnswer = correctIndex;
        } else {
            if (correctAnswers.length === 0) { alert('Выберите хотя бы один правильный ответ'); return; }
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

    const handleTaskClick = (task: Task) => {
        const takenCourseId = searchParams.get('takenCourseId');
        const courseId = searchParams.get('courseId');
        const params = new URLSearchParams();
        if (takenCourseId) params.set('takenCourseId', takenCourseId);
        if (courseId) params.set('courseId', courseId);
        const query = params.toString() ? `?${params.toString()}` : '';
        navigate(`/task/${task.id}${query}`);
    };

    if (loading) return <div style={{ padding: '20px' }}>Загрузка задач...</div>;

    const completedCount = tasks.filter(t => t.isCompleted).length;

    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
            <button onClick={handleBack} style={{ marginBottom: '16px' }}>← Назад к курсу</button>

            <h1>Модуль #{moduleId}</h1>

            {!isAdmin && tasks.length > 0 && (
                <div style={{ marginBottom: '20px', padding: '12px 16px', background: '#f8f9fa', borderRadius: '8px', border: '1px solid #e9ecef' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                        <span style={{ fontSize: '14px', color: '#555' }}>Прогресс модуля</span>
                        <span style={{ fontSize: '14px', color: '#555' }}>
                            {completedCount} / {tasks.length} задач
                        </span>
                    </div>
                    <div style={{ background: '#e9ecef', borderRadius: '4px', height: '8px', overflow: 'hidden' }}>
                        <div style={{
                            background: completedCount === tasks.length ? '#28a745' : '#007bff',
                            height: '100%',
                            width: `${tasks.length > 0 ? (completedCount / tasks.length * 100) : 0}%`,
                            transition: 'width 0.3s'
                        }} />
                    </div>
                </div>
            )}

            {isAdmin && (
                <button onClick={() => setShowCreateForm(!showCreateForm)} style={{ marginBottom: '20px', padding: '8px 16px' }}>
                    {showCreateForm ? '❌ Отмена' : '+ Создать задачу'}
                </button>
            )}

            {isAdmin && showCreateForm && (
                <div style={{ border: '1px solid #007bff', borderRadius: '8px', padding: '20px', marginBottom: '20px', background: '#f8f9fa' }}>
                    <h3>Новая задача</h3>
                    <div style={{ marginBottom: '16px' }}>
                        <label>Тип задачи:</label>
                        <select value={taskType} onChange={(e) => setTaskType(e.target.value as any)} style={{ marginLeft: '8px', padding: '4px' }}>
                            <option value="ONE_POSSIBLE_ANSWER">Один правильный ответ</option>
                            <option value="MULTIPLE">Несколько правильных ответов</option>
                        </select>
                    </div>
                    <div style={{ marginBottom: '16px' }}>
                        <label>Вопрос:</label>
                        <textarea value={question} onChange={(e) => setQuestion(e.target.value)} placeholder="Введите вопрос..." style={{ width: '100%', padding: '8px', marginTop: '4px', minHeight: '60px' }} />
                    </div>
                    <div style={{ marginBottom: '16px' }}>
                        <label>Варианты ответов:</label>
                        {options.map((opt, idx) => (
                            <div key={idx} style={{ display: 'flex', alignItems: 'center', marginTop: '8px', gap: '8px' }}>
                                <input
                                    type={taskType === 'ONE_POSSIBLE_ANSWER' ? 'radio' : 'checkbox'}
                                    name="correct"
                                    checked={taskType === 'ONE_POSSIBLE_ANSWER' ? correctIndex === idx : correctAnswers.includes(idx)}
                                    onChange={() => handleCorrectAnswerToggle(idx)}
                                />
                                <input type="text" value={opt} onChange={(e) => handleOptionChange(idx, e.target.value)} placeholder={`Вариант ${idx + 1}`} style={{ flex: 1, padding: '6px' }} />
                                <button onClick={() => handleRemoveOption(idx)} disabled={options.length <= 2}>✖</button>
                            </div>
                        ))}
                        <button onClick={handleAddOption} style={{ marginTop: '8px' }}>+ Добавить вариант</button>
                    </div>
                    <button onClick={handleCreateTask} disabled={creating} style={{ padding: '8px 16px', background: '#28a745', color: 'white', border: 'none', borderRadius: '4px' }}>
                        {creating ? 'Создание...' : 'Создать задачу'}
                    </button>
                </div>
            )}

            <h2>Задачи модуля</h2>

            {tasks.length === 0 ? (
                <p>{isAdmin ? 'В модуле пока нет задач. Создайте первую задачу!' : 'В модуле пока нет задач.'}</p>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {tasks.map((task) => (
                        <div
                            key={task.id}
                            style={{
                                border: task.isCompleted ? '2px solid #28a745' : '1px solid #ddd',
                                borderRadius: '8px',
                                padding: '16px',
                                cursor: 'pointer',
                                transition: 'box-shadow 0.2s',
                                background: task.isCompleted ? '#f0fff4' : '#fff',
                            }}
                            onClick={() => handleTaskClick(task)}
                            onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)'}
                            onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'none'}
                        >
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div>
                                    <strong>Задача #{task.id}</strong>
                                    <span style={{ marginLeft: '8px', fontSize: '12px', background: '#e9ecef', padding: '2px 6px', borderRadius: '4px' }}>
                                        {task.content.type === 'ONE_POSSIBLE_ANSWER' ? 'Один ответ' : 'Множественный выбор'}
                                    </span>
                                    {task.isCompleted && (
                                        <span style={{ marginLeft: '8px', fontSize: '12px', background: '#28a745', color: 'white', padding: '2px 8px', borderRadius: '4px' }}>
                                            ✓ Решено
                                        </span>
                                    )}
                                    <p style={{ margin: '8px 0 0 0', color: '#333' }}>{task.content.question}</p>
                                </div>
                                <span style={{ color: task.isCompleted ? '#28a745' : '#333' }}>→</span>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default ModulePage;

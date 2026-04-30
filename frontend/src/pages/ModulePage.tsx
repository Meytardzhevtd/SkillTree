import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useSearchParams, useLocation } from 'react-router-dom';
import {
    getTasksByModuleId,
    createTask,
    getMyRoleInCourse,
    startModule,
    getLessonsByModuleId,
    createLesson,
    deleteLesson
} from '../services/courseApi';

interface Lesson {
    id: number;
    title: string;
    content: string;
}

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

type NavItem = { type: 'lesson'; id: number; title: string } | { type: 'task'; id: number; title: string };

const ModulePage: React.FC = () => {
    const { moduleId } = useParams<{ moduleId: string }>();
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const location = useLocation();

    const [lessons, setLessons] = useState<Lesson[]>([]);
    const [tasks, setTasks] = useState<Task[]>([]);
    const [navItems, setNavItems] = useState<NavItem[]>([]);
    const [currentNavIndex, setCurrentNavIndex] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [isAdmin, setIsAdmin] = useState(false);

    // Форма создания урока
    const [showLessonForm, setShowLessonForm] = useState(false);
    const [newLessonTitle, setNewLessonTitle] = useState('');
    const [newLessonContent, setNewLessonContent] = useState('');
    const [creating, setCreating] = useState(false);

    // Форма создания задачи
    const [showTaskForm, setShowTaskForm] = useState(false);
    const [taskType, setTaskType] = useState<'ONE_POSSIBLE_ANSWER' | 'MULTIPLE'>('ONE_POSSIBLE_ANSWER');
    const [question, setQuestion] = useState('');
    const [options, setOptions] = useState<string[]>(['', '']);
    const [correctIndex, setCorrectIndex] = useState(0);
    const [correctAnswers, setCorrectAnswers] = useState<number[]>([]);
    const [taskScore, setTaskScore] = useState<number>(10);

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

            const [lessonsData, tasksData] = await Promise.all([
                getLessonsByModuleId(Number(moduleId)),
                getTasksByModuleId(Number(moduleId), pmId ?? undefined)
            ]);

            setLessons(lessonsData);
            setTasks(tasksData);

            // Собираем навигацию: сначала все уроки, потом все задачи
            const items: NavItem[] = [
                ...lessonsData.map(l => ({ type: 'lesson' as const, id: l.id, title: l.title })),
                ...tasksData.map(t => ({ type: 'task' as const, id: t.id, title: t.content.question }))
            ];
            setNavItems(items);
            setCurrentNavIndex(items.length > 0 ? 0 : null);

        } catch (err) {
            console.error('Ошибка загрузки:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateLesson = async () => {
        if (!newLessonTitle.trim()) {
            alert('Введите название урока');
            return;
        }
        if (!newLessonContent.trim()) {
            alert('Введите содержание урока');
            return;
        }

        console.log('Sending lesson:', {
            moduleId: Number(moduleId),
            title: newLessonTitle,
            content: newLessonContent
        });

        try {
            setCreating(true);
            await createLesson(Number(moduleId), newLessonTitle, newLessonContent);
            setNewLessonTitle('');
            setNewLessonContent('');
            setShowLessonForm(false);
            await loadAll();
        } catch (err) {
            console.error('Ошибка создания урока:', err);
            alert('Ошибка при создании урока');
        } finally {
            setCreating(false);
        }
    };

    const handleDeleteLesson = async (lessonId: number) => {
        if (!confirm('Удалить урок?')) return;
        try {
            await deleteLesson(lessonId);
            await loadAll();
        } catch (err) {
            console.error('Ошибка удаления урока:', err);
            alert('Ошибка при удалении урока');
        }
    };

    const handlePrev = () => {
        if (currentNavIndex !== null && currentNavIndex > 0) {
            const prevItem = navItems[currentNavIndex - 1];
            if (prevItem.type === 'lesson') {
                navigate(`/lesson/${prevItem.id}`);
            } else {
                const takenCourseId = searchParams.get('takenCourseId');
                const courseId = searchParams.get('courseId');
                const params = new URLSearchParams();
                if (takenCourseId) params.set('takenCourseId', takenCourseId);
                if (courseId) params.set('courseId', courseId);
                navigate(`/task/${prevItem.id}?${params.toString()}`);
            }
            setCurrentNavIndex(currentNavIndex - 1);
        }
    };

    const handleNext = () => {
        if (currentNavIndex !== null && currentNavIndex < navItems.length - 1) {
            const nextItem = navItems[currentNavIndex + 1];
            if (nextItem.type === 'lesson') {
                navigate(`/lesson/${nextItem.id}`);
            } else {
                const takenCourseId = searchParams.get('takenCourseId');
                const courseId = searchParams.get('courseId');
                const params = new URLSearchParams();
                if (takenCourseId) params.set('takenCourseId', takenCourseId);
                if (courseId) params.set('courseId', courseId);
                navigate(`/task/${nextItem.id}?${params.toString()}`);
            }
            setCurrentNavIndex(currentNavIndex + 1);
        }
    };

    const handleGoToFirstTask = () => {
        const firstTaskIndex = navItems.findIndex(item => item.type === 'task');
        if (firstTaskIndex !== -1) {
            const taskItem = navItems[firstTaskIndex];
            const takenCourseId = searchParams.get('takenCourseId');
            const courseId = searchParams.get('courseId');
            const params = new URLSearchParams();
            if (takenCourseId) params.set('takenCourseId', takenCourseId);
            if (courseId) params.set('courseId', courseId);
            navigate(`/task/${taskItem.id}?${params.toString()}`);
            setCurrentNavIndex(firstTaskIndex);
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

    // Handlers для задачи
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
            await createTask(Number(moduleId), taskType === 'ONE_POSSIBLE_ANSWER' ? 1 : 2, content, taskScore);
            setQuestion('');
            setOptions(['', '']);
            setCorrectIndex(0);
            setCorrectAnswers([]);
            setTaskScore(10);
            setShowTaskForm(false);
            await loadAll();
        } catch (err) {
            console.error('Ошибка создания задачи:', err);
            alert('Ошибка при создании задачи');
        } finally {
            setCreating(false);
        }
    };

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;

    const completedCount = tasks.filter(t => t.isCompleted).length;
    const currentItem = currentNavIndex !== null ? navItems[currentNavIndex] : null;
    const isCurrentLesson = currentItem?.type === 'lesson';
    const isLastLesson = currentItem?.type === 'lesson' && navItems[currentNavIndex! + 1]?.type === 'task';
    const isLastItem = currentNavIndex === navItems.length - 1;

    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
            <button onClick={handleBack} style={{ marginBottom: '16px' }}>← Назад к курсу</button>

            <h1>Модуль #{moduleId}</h1>

            {/* Прогресс модуля */}
            {!isAdmin && tasks.length > 0 && (
                <div style={{ marginBottom: '20px', padding: '12px 16px', background: '#f8f9fa', borderRadius: '8px', border: '1px solid #e9ecef' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                        <span style={{ fontSize: '14px', color: '#555' }}>Прогресс модуля</span>
                        <span style={{ fontSize: '14px', color: '#555' }}>{completedCount} / {tasks.length} задач</span>
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

            {/* Навигационные кнопки */}
            {navItems.length > 0 && currentItem && (
                <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '20px',
                    padding: '12px',
                    background: '#f5f5f5',
                    borderRadius: '8px'
                }}>
                    {/* <button onClick={handlePrev} disabled={currentNavIndex === 0}>◀ Пред.</button>
                    <span style={{ fontSize: '14px', color: '#666' }}>
                        {currentNavIndex! + 1} / {navItems.length} ·
                        <span style={{ marginLeft: '8px', color: isCurrentLesson ? '#28a745' : '#007bff' }}>
                            {isCurrentLesson ? '📖 Урок' : '📝 Задача'}
                        </span>
                    </span>
                    <button onClick={handleNext} disabled={isLastItem}>След. ▶</button> */}
                </div>
            )}

            {/* Кнопка перехода к задачам (показывается только на последнем уроке) */}
            {isLastLesson && (
                <div style={{ textAlign: 'center', marginBottom: '20px' }}>
                    <button
                        onClick={handleGoToFirstTask}
                        style={{ padding: '8px 24px', background: '#28a745', color: 'white', border: 'none', borderRadius: '6px', fontSize: '15px' }}
                    >
                        Перейти к задачам →
                    </button>
                </div>
            )}

            {/* Кнопки создания для админа */}
            {isAdmin && (
                <div style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
                    <button onClick={() => { setShowLessonForm(!showLessonForm); setShowTaskForm(false); }}>
                        {showLessonForm ? '❌ Отмена' : '+ Новый урок'}
                    </button>
                    <button onClick={() => { setShowTaskForm(!showTaskForm); setShowLessonForm(false); }}>
                        {showTaskForm ? '❌ Отмена' : '+ Новая задача'}
                    </button>
                </div>
            )}

            {/* Форма создания урока */}
            {isAdmin && showLessonForm && (
                <div style={{ border: '1px solid #28a745', borderRadius: '8px', padding: '20px', marginBottom: '24px', background: '#f0fff4' }}>
                    <h3 style={{ marginTop: 0 }}>Новый урок</h3>
                    <div style={{ marginBottom: '12px' }}>
                        <label>Название:</label>
                        <input
                            type="text"
                            value={newLessonTitle}
                            onChange={(e) => setNewLessonTitle(e.target.value)}
                            style={{ width: '100%', padding: '8px', marginTop: '4px' }}
                        />
                    </div>
                    <div style={{ marginBottom: '12px' }}>
                        <label>Содержание (поддерживается Markdown):</label>
                        <textarea
                            value={newLessonContent}
                            onChange={(e) => setNewLessonContent(e.target.value)}
                            placeholder="# Заголовок&#10;Текст урока..."
                            style={{ width: '100%', padding: '8px', marginTop: '4px', minHeight: '150px' }}
                        />
                    </div>
                    <button onClick={handleCreateLesson} disabled={creating}>
                        {creating ? 'Создание...' : 'Создать урок'}
                    </button>
                </div>
            )}

            {/* Форма создания задачи */}
            {isAdmin && showTaskForm && (
                <div style={{ border: '1px solid #007bff', borderRadius: '8px', padding: '20px', marginBottom: '24px', background: '#f8f9fa' }}>
                    <h3 style={{ marginTop: 0 }}>Новая задача</h3>
                    <div style={{ marginBottom: '12px' }}>
                        <label>Тип задачи:</label>
                        <select value={taskType} onChange={(e) => setTaskType(e.target.value as any)} style={{ marginLeft: '8px', padding: '4px' }}>
                            <option value="ONE_POSSIBLE_ANSWER">Один правильный ответ</option>
                            <option value="MULTIPLE">Несколько правильных ответов</option>
                        </select>
                    </div>
                    <div style={{ marginBottom: '12px' }}>
                        <label>Вопрос:</label>
                        <textarea value={question} onChange={(e) => setQuestion(e.target.value)} placeholder="Введите вопрос..." style={{ width: '100%', padding: '8px', minHeight: '60px' }} />
                    </div>
                    <div style={{ marginBottom: '12px' }}>
                        <label>Варианты ответов:</label>
                        {options.map((opt, idx) => (
                            <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '6px' }}>
                                <input type={taskType === 'ONE_POSSIBLE_ANSWER' ? 'radio' : 'checkbox'} checked={taskType === 'ONE_POSSIBLE_ANSWER' ? correctIndex === idx : correctAnswers.includes(idx)} onChange={() => handleCorrectAnswerToggle(idx)} />
                                <input type="text" value={opt} onChange={(e) => handleOptionChange(idx, e.target.value)} placeholder={`Вариант ${idx + 1}`} style={{ flex: 1, padding: '6px' }} />
                                <button onClick={() => handleRemoveOption(idx)} disabled={options.length <= 2}>✖</button>
                            </div>
                        ))}
                        <button onClick={handleAddOption} style={{ marginTop: '8px' }}>+ Добавить вариант</button>
                    </div>
                    <div style={{ marginBottom: '12px' }}>
                        <label>Баллы за задачу:</label>
                        <input
                            type="number"
                            min="1"
                            max="100"
                            value={taskScore}
                            onChange={(e) => setTaskScore(Number(e.target.value))}
                            style={{ width: '100%', padding: '8px', marginTop: '4px' }}
                        />
                    </div>
                    <button onClick={handleCreateTask} disabled={creating} style={{ background: '#28a745', color: 'white', border: 'none', padding: '8px 16px', borderRadius: '4px' }}>
                        {creating ? 'Создание...' : 'Создать задачу'}
                    </button>
                </div>
            )}

            {/* Список уроков (кликабельные) */}
            <h2>📖 Уроки</h2>
            {lessons.length === 0 && <p style={{ color: '#888' }}>В модуле пока нет уроков.</p>}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '32px' }}>
                {lessons.map((lesson) => (
                    <div
                        key={lesson.id}
                        style={{
                            border: '1px solid #ddd',
                            borderRadius: '8px',
                            padding: '16px',
                            background: '#fff',
                            cursor: 'pointer',
                            transition: 'box-shadow 0.2s'
                        }}
                        onClick={() => navigate(`/lesson/${lesson.id}`)}
                        onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)'}
                        onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'none'}
                    >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <strong style={{ fontSize: '16px' }}>{lesson.title}</strong>
                            {isAdmin && (
                                <button
                                    onClick={(e) => { e.stopPropagation(); handleDeleteLesson(lesson.id); }}
                                    style={{ background: '#dc3545', color: 'white', border: 'none', padding: '4px 10px', borderRadius: '4px' }}
                                >
                                    Удалить
                                </button>
                            )}
                        </div>
                        <p style={{ margin: '8px 0 0 0', color: '#666', whiteSpace: 'pre-wrap' }}>
                            {lesson.content.length > 200 ? lesson.content.substring(0, 200) + '...' : lesson.content}
                        </p>
                    </div>
                ))}
            </div>

            {/* Список задач (как было) */}
            <h2>📝 Задачи</h2>
            {tasks.length === 0 && (
                <p style={{ color: '#888' }}>{isAdmin ? 'В модуле пока нет задач. Создайте первую задачу!' : 'В модуле пока нет задач.'}</p>
            )}
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
                        onClick={() => {
                            const takenCourseId = searchParams.get('takenCourseId');
                            const courseId = searchParams.get('courseId');
                            const params = new URLSearchParams();
                            if (takenCourseId) params.set('takenCourseId', takenCourseId);
                            if (courseId) params.set('courseId', courseId);
                            navigate(`/task/${task.id}?${params.toString()}`);
                        }}
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
        </div>
    );
};

export default ModulePage;
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createCourse, createModule, createTask } from '../services/courseApi';

type TaskType = 'ONE_POSSIBLE_ANSWER' | 'MULTIPLE';

interface TaskDraft {
    taskType: TaskType;
    question: string;
    options: string[];
    correctIndex: number;
    correctAnswers: number[];
    score: number;
}

interface ModuleDraft {
    name: string;
    tasks: TaskDraft[];
}

const emptyTask = (): TaskDraft => ({
    taskType: 'ONE_POSSIBLE_ANSWER',
    question: '',
    options: ['', ''],
    correctIndex: 0,
    correctAnswers: [],
    score: 10,
});

const CreateCoursePage: React.FC = () => {
    const navigate = useNavigate();

    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [modules, setModules] = useState<ModuleDraft[]>([]);
    const [moduleName, setModuleName] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const [activeModuleIdx, setActiveModuleIdx] = useState<number | null>(null);
    const [taskDraft, setTaskDraft] = useState<TaskDraft>(emptyTask());

    const handleAddModule = () => {
        if (!moduleName.trim()) return;
        setModules([...modules, { name: moduleName, tasks: [] }]);
        setModuleName('');
    };

    const handleRemoveModule = (idx: number) => {
        setModules(modules.filter((_, i) => i !== idx));
        if (activeModuleIdx === idx) setActiveModuleIdx(null);
    };

    const handleOptionChange = (idx: number, value: string) => {
        const opts = [...taskDraft.options];
        opts[idx] = value;
        setTaskDraft({ ...taskDraft, options: opts });
    };

    const handleAddOption = () => {
        setTaskDraft({ ...taskDraft, options: [...taskDraft.options, ''] });
    };

    const handleRemoveOption = (idx: number) => {
        if (taskDraft.options.length <= 2) return;
        const opts = taskDraft.options.filter((_, i) => i !== idx);
        setTaskDraft({
            ...taskDraft,
            options: opts,
            correctIndex: taskDraft.correctIndex >= opts.length ? 0 : taskDraft.correctIndex,
            correctAnswers: taskDraft.correctAnswers
                .filter(i => i !== idx)
                .map(i => (i > idx ? i - 1 : i)),
        });
    };

    const handleCorrectToggle = (idx: number) => {
        if (taskDraft.taskType === 'ONE_POSSIBLE_ANSWER') {
            setTaskDraft({ ...taskDraft, correctIndex: idx });
        } else {
            const ca = taskDraft.correctAnswers.includes(idx)
                ? taskDraft.correctAnswers.filter(i => i !== idx)
                : [...taskDraft.correctAnswers, idx];
            setTaskDraft({ ...taskDraft, correctAnswers: ca });
        }
    };

    const handleTaskTypeChange = (type: TaskType) => {
        setTaskDraft({ ...taskDraft, taskType: type, correctIndex: 0, correctAnswers: [] });
    };

    const handleAddTask = () => {
        if (activeModuleIdx === null) return;
        if (!taskDraft.question.trim()) { alert('Введите вопрос'); return; }
        const filtered = taskDraft.options.filter(o => o.trim() !== '');
        if (filtered.length < 2) { alert('Добавьте минимум 2 варианта ответа'); return; }
        if (taskDraft.taskType === 'MULTIPLE' && taskDraft.correctAnswers.length === 0) {
            alert('Выберите хотя бы один правильный ответ'); return;
        }

        const newModules = modules.map((m, i) => {
            if (i !== activeModuleIdx) return m;
            return { ...m, tasks: [...m.tasks, { ...taskDraft, options: filtered, score: taskDraft.score }] };
        });
        setModules(newModules);
        setTaskDraft(emptyTask());
    };

    const handleRemoveTask = (moduleIdx: number, taskIdx: number) => {
        const newModules = modules.map((m, i) => {
            if (i !== moduleIdx) return m;
            return { ...m, tasks: m.tasks.filter((_, ti) => ti !== taskIdx) };
        });
        setModules(newModules);
    };

    const handleSubmit = async () => {
        if (!name.trim()) { alert('Введите название курса'); return; }
        if (modules.length === 0) { alert('Добавьте хотя бы один модуль'); return; }

        setIsLoading(true);
        try {
            const course = await createCourse(name, description);
            const courseId = course.id;

            for (const moduleDraft of modules) {
                const module = await createModule(courseId, moduleDraft.name, false);
                const moduleId = module.moduleId;

                for (const task of moduleDraft.tasks) {
                    const filtered = task.options.filter(o => o.trim() !== '');
                    const content: any = {
                        type: task.taskType,
                        question: task.question,
                        options: filtered,
                        score: task.score,
                    };
                    if (task.taskType === 'ONE_POSSIBLE_ANSWER') {
                        content.indexCorrectAnswer = task.correctIndex;
                    } else {
                        content.correctAnswers = task.correctAnswers;
                    }
                    const typeId = task.taskType === 'ONE_POSSIBLE_ANSWER' ? 1 : 2;
                    await await createTask(moduleId, typeId, content, task.score);
                }
            }

            navigate(`/course/${courseId}`);
        } catch (err: any) {
            console.error(err);
            alert(err.response?.data || 'Ошибка при создании курса');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
            <h2>Создать новый курс</h2>

            <div style={{ marginBottom: '16px' }}>
                <input
                    type="text"
                    placeholder="Название курса"
                    value={name}
                    onChange={e => setName(e.target.value)}
                    disabled={isLoading}
                    style={{ width: '100%', padding: '8px', marginBottom: '8px' }}
                />
                <textarea
                    placeholder="Описание курса"
                    value={description}
                    onChange={e => setDescription(e.target.value)}
                    disabled={isLoading}
                    style={{ width: '100%', padding: '8px', minHeight: '80px' }}
                />
            </div>

            <hr />

            <h3>Модули</h3>
            <div style={{ display: 'flex', gap: '8px', marginBottom: '16px' }}>
                <input
                    type="text"
                    placeholder="Название модуля"
                    value={moduleName}
                    onChange={e => setModuleName(e.target.value)}
                    disabled={isLoading}
                    style={{ flex: 1, padding: '8px' }}
                    onKeyDown={e => e.key === 'Enter' && handleAddModule()}
                />
                <button onClick={handleAddModule} disabled={isLoading}>
                    + Добавить модуль
                </button>
            </div>

            {modules.map((m, mIdx) => (
                <div key={mIdx} style={{ border: '1px solid #ddd', borderRadius: '8px', padding: '16px', marginBottom: '12px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                        <strong style={{ fontSize: '16px' }}>📚 {m.name}</strong>
                        <button
                            onClick={() => handleRemoveModule(mIdx)}
                            disabled={isLoading}
                            style={{ background: '#dc3545', color: 'white', border: 'none', padding: '4px 10px', borderRadius: '4px' }}
                        >
                            Удалить модуль
                        </button>
                    </div>

                    {m.tasks.length > 0 && (
                        <div style={{ marginBottom: '12px' }}>
                            {m.tasks.map((t, tIdx) => (
                                <div key={tIdx} style={{ background: '#f8f9fa', borderRadius: '6px', padding: '10px', marginBottom: '6px', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                    <div>
                                        <span style={{ fontSize: '12px', background: '#e9ecef', padding: '2px 6px', borderRadius: '4px', marginRight: '8px' }}>
                                            {t.taskType === 'ONE_POSSIBLE_ANSWER' ? 'Один ответ' : 'Множественный выбор'}
                                        </span>
                                        <span>{t.question}</span>
                                        <div style={{ marginTop: '4px', fontSize: '12px', color: '#666' }}>
                                            {t.options.filter(o => o.trim()).map((o, oi) => {
                                                const isCorrect = t.taskType === 'ONE_POSSIBLE_ANSWER'
                                                    ? t.correctIndex === oi
                                                    : t.correctAnswers.includes(oi);
                                                return (
                                                    <span key={oi} style={{ marginRight: '8px', color: isCorrect ? '#28a745' : 'inherit' }}>
                                                        {isCorrect ? '✓' : '○'} {o}
                                                    </span>
                                                );
                                            })}
                                        </div>
                                    </div>
                                    <button
                                        onClick={() => handleRemoveTask(mIdx, tIdx)}
                                        disabled={isLoading}
                                        style={{ background: 'none', border: '1px solid #dc3545', color: '#dc3545', padding: '2px 8px', borderRadius: '4px', cursor: 'pointer', flexShrink: 0 }}
                                    >
                                        ✖
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}

                    <button
                        onClick={() => setActiveModuleIdx(activeModuleIdx === mIdx ? null : mIdx)}
                        disabled={isLoading}
                        style={{ padding: '6px 14px', fontSize: '13px' }}
                    >
                        {activeModuleIdx === mIdx ? '❌ Отмена' : '+ Добавить задачу'}
                    </button>

                    {activeModuleIdx === mIdx && (
                        <div style={{ marginTop: '12px', padding: '16px', border: '1px solid #007bff', borderRadius: '8px', background: '#f0f4ff', maxHeight: '500px', overflowY: 'auto' }}>
                            <div style={{ marginBottom: '12px' }}>
                                <label>Тип задачи: </label>
                                <select
                                    value={taskDraft.taskType}
                                    onChange={e => handleTaskTypeChange(e.target.value as TaskType)}
                                    style={{ padding: '4px', marginLeft: '8px' }}
                                >
                                    <option value="ONE_POSSIBLE_ANSWER">Один правильный ответ</option>
                                    <option value="MULTIPLE">Несколько правильных ответов</option>
                                </select>
                            </div>

                            <div style={{ marginBottom: '12px' }}>
                                <label>Вопрос:</label>
                                <textarea
                                    value={taskDraft.question}
                                    onChange={e => setTaskDraft({ ...taskDraft, question: e.target.value })}
                                    placeholder="Введите вопрос..."
                                    style={{ width: '100%', padding: '8px', marginTop: '4px', minHeight: '60px' }}
                                />
                            </div>

                            <div style={{ marginBottom: '12px' }}>
                                <label>Варианты ответов:</label>
                                {taskDraft.options.map((opt, oi) => (
                                    <div key={oi} style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '6px' }}>
                                        <input
                                            type={taskDraft.taskType === 'ONE_POSSIBLE_ANSWER' ? 'radio' : 'checkbox'}
                                            name="correct"
                                            checked={taskDraft.taskType === 'ONE_POSSIBLE_ANSWER'
                                                ? taskDraft.correctIndex === oi
                                                : taskDraft.correctAnswers.includes(oi)}
                                            onChange={() => handleCorrectToggle(oi)}
                                        />
                                        <input
                                            type="text"
                                            value={opt}
                                            onChange={e => handleOptionChange(oi, e.target.value)}
                                            placeholder={`Вариант ${oi + 1}`}
                                            style={{ flex: 1, padding: '6px' }}
                                        />
                                        <button
                                            onClick={() => handleRemoveOption(oi)}
                                            disabled={taskDraft.options.length <= 2}
                                            style={{ padding: '4px 8px' }}
                                        >
                                            ✖
                                        </button>
                                    </div>
                                ))}
                                <button onClick={handleAddOption} style={{ marginTop: '8px', fontSize: '13px' }}>
                                    + Добавить вариант
                                </button>
                            </div>

                            <div style={{ marginBottom: '12px' }}>
                                <label>Баллы за задачу:</label>
                                <input
                                    type="number"
                                    min="1"
                                    max="100"
                                    value={taskDraft.score}
                                    onChange={(e) => setTaskDraft({ ...taskDraft, score: Number(e.target.value) })}
                                    style={{ width: '100%', padding: '8px', marginTop: '4px' }}
                                />
                            </div>

                            <button
                                onClick={handleAddTask}
                                style={{ padding: '8px 16px', background: '#28a745', color: 'white', border: 'none', borderRadius: '4px' }}
                            >
                                Добавить задачу
                            </button>
                        </div>
                    )}
                </div>
            ))}

            <hr />

            <button
                onClick={handleSubmit}
                disabled={isLoading}
                style={{ padding: '10px 24px', background: '#007bff', color: 'white', border: 'none', borderRadius: '6px', fontSize: '15px' }}
            >
                {isLoading ? 'Создание...' : 'Создать курс'}
            </button>
        </div>
    );
};

export default CreateCoursePage;
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createCourse, createModule, createTask } from '../services/courseApi';

const CreateCoursePage: React.FC = () => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [modules, setModules] = useState<{ name: string; tasks: { content: any }[] }[]>([]);
  const [moduleName, setModuleName] = useState('');
  const [taskContent, setTaskContent] = useState('');
  const [currentModuleIndex, setCurrentModuleIndex] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();

  const handleAddModule = () => {
    if (!moduleName.trim()) return;
    setModules([...modules, { name: moduleName, tasks: [] }]);
    setModuleName('');
  };

  const handleAddTask = () => {
    if (currentModuleIndex === null || !taskContent.trim()) return;
    const newModules = [...modules];
    newModules[currentModuleIndex].tasks.push({ content: { text: taskContent } });
    setModules(newModules);
    setTaskContent('');
  };

  const handleSubmit = async () => {
    if (!name.trim()) return alert('Введите название курса');
    if (modules.length === 0) return alert('Добавьте хотя бы один модуль');

    setIsLoading(true);

    try {
      const course = await createCourse(name, description);
      const courseId = course.id;

      for (const moduleData of modules) {
        const module = await createModule(courseId, moduleData.name, false);
        const moduleId = module.moduleId;

        for (const taskData of moduleData.tasks) {
          await createTask(moduleId, 1, taskData.content);
        }
      }

      navigate('/dashboard');
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data || 'Ошибка при создании курса');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2>Создать новый курс</h2>

      <div style={{ marginBottom: '16px' }}>
        <input
          type="text"
          placeholder="Название курса"
          value={name}
          onChange={e => setName(e.target.value)}
          disabled={isLoading}
          style={{ width: '300px', padding: '8px', marginRight: '8px' }}
        />
      </div>

      <div style={{ marginBottom: '16px' }}>
        <textarea
          placeholder="Описание курса"
          value={description}
          onChange={e => setDescription(e.target.value)}
          disabled={isLoading}
          style={{ width: '300px', padding: '8px', minHeight: '80px' }}
        />
      </div>

      <hr />

      <h3>Модули</h3>
      <div style={{ marginBottom: '16px' }}>
        <input
          type="text"
          placeholder="Название модуля"
          value={moduleName}
          onChange={e => setModuleName(e.target.value)}
          disabled={isLoading}
          style={{ padding: '8px', marginRight: '8px' }}
        />
        <button onClick={handleAddModule} disabled={isLoading}>
          Добавить модуль
        </button>
      </div>

      {modules.length > 0 && (
        <ul style={{ marginBottom: '16px' }}>
          {modules.map((m, idx) => (
            <li key={idx} style={{ marginBottom: '12px' }}>
              <strong>{m.name}</strong>
              <button
                onClick={() => setCurrentModuleIndex(idx)}
                disabled={isLoading}
                style={{ marginLeft: '8px' }}
              >
                {currentModuleIndex === idx ? 'Текущий' : 'Выбрать для задач'}
              </button>
              {m.tasks.length > 0 && (
                <ul>
                  {m.tasks.map((t, tidx) => (
                    <li key={tidx}>📝 {JSON.stringify(t.content)}</li>
                  ))}
                </ul>
              )}
            </li>
          ))}
        </ul>
      )}

      {currentModuleIndex !== null && modules[currentModuleIndex] && (
        <div style={{ marginBottom: '16px', padding: '12px', border: '1px solid #ccc', borderRadius: '4px' }}>
          <h4>Добавить задачу в модуль "{modules[currentModuleIndex].name}"</h4>
          <input
            type="text"
            placeholder="Текст задачи"
            value={taskContent}
            onChange={e => setTaskContent(e.target.value)}
            disabled={isLoading}
            style={{ padding: '8px', marginRight: '8px', width: '300px' }}
          />
          <button onClick={handleAddTask} disabled={isLoading}>
            Добавить задачу
          </button>
        </div>
      )}

      <hr />
      <button onClick={handleSubmit} disabled={isLoading} style={{ padding: '8px 16px' }}>
        {isLoading ? 'Создание...' : 'Создать курс'}
      </button>
    </div>
  );
};

export default CreateCoursePage;
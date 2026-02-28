import React, { useState } from 'react';
import { createFullCourse } from '../services/courseApi';
import { getUser } from '../services/authStorage';

const CreateCoursePage: React.FC = () => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [modules, setModules] = useState<{ name: string; tasks: string[] }[]>([]);
  const [moduleName, setModuleName] = useState('');
  const [taskContent, setTaskContent] = useState('');
  const [currentModuleIndex, setCurrentModuleIndex] = useState<number | null>(null);

  const user = getUser();

  const handleAddModule = () => {
    setModules([...modules, { name: moduleName, tasks: [] }]);
    setModuleName('');
  };

  const handleAddTask = () => {
    if (currentModuleIndex === null) return;
    const newModules = [...modules];
    newModules[currentModuleIndex].tasks.push(taskContent);
    setModules(newModules);
    setTaskContent('');
  };

  const handleSubmit = async () => {
    if (!user) return alert('Необходима авторизация');

    const payload = {
      userId: user.id,
      name,
      description,
      modules: modules.map(m => ({
        name: m.name,
        tasks: m.tasks.map(t => ({ content: t })),
      })),
    };

    try {
      await createFullCourse(payload);
      alert('Курс создан!');
      setName('');
      setDescription('');
      setModules([]);
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data || 'Ошибка при создании курса');
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2>Создать новый курс</h2>
      <input
        type="text"
        placeholder="Название курса"
        value={name}
        onChange={e => setName(e.target.value)}
      />
      <br />
      <textarea
        placeholder="Описание курса"
        value={description}
        onChange={e => setDescription(e.target.value)}
      />
      <br />
      <hr />
      <h3>Модули</h3>
      <input
        type="text"
        placeholder="Название модуля"
        value={moduleName}
        onChange={e => setModuleName(e.target.value)}
      />
      <button onClick={handleAddModule}>Добавить модуль</button>

      <ul>
        {modules.map((m, idx) => (
          <li key={idx}>
            <b>{m.name}</b>
            <button onClick={() => setCurrentModuleIndex(idx)}>Выбрать для задач</button>
            <ul>
              {m.tasks.map((t, tidx) => (
                <li key={tidx}>{t}</li>
              ))}
            </ul>
          </li>
        ))}
      </ul>

      {currentModuleIndex !== null && (
        <div>
          <h4>Добавить задачу в модуль "{modules[currentModuleIndex].name}"</h4>
          <input
            type="text"
            placeholder="Текст задачи"
            value={taskContent}
            onChange={e => setTaskContent(e.target.value)}
          />
          <button onClick={handleAddTask}>Добавить задачу</button>
        </div>
      )}

      <hr />
      <button onClick={handleSubmit}>Создать курс</button>
    </div>
  );
};

export default CreateCoursePage;
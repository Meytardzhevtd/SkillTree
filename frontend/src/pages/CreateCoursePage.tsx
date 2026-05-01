import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createCourse } from '../services/courseApi';

const CreateCoursePage: React.FC = () => {
    const navigate = useNavigate();

    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!name.trim()) {
            alert('Введите название курса');
            return;
        }

        setIsLoading(true);
        try {
            const course = await createCourse(name, description);
            navigate(`/course/${course.id}`);
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

            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '16px' }}>
                    <label>Название курса *</label>
                    <input
                        type="text"
                        placeholder="Название курса"
                        value={name}
                        onChange={e => setName(e.target.value)}
                        disabled={isLoading}
                        style={{ width: '100%', padding: '8px', marginTop: '4px' }}
                        required
                    />
                </div>

                <div style={{ marginBottom: '16px' }}>
                    <label>Описание курса</label>
                    <textarea
                        placeholder="Описание курса"
                        value={description}
                        onChange={e => setDescription(e.target.value)}
                        disabled={isLoading}
                        style={{ width: '100%', padding: '8px', marginTop: '4px', minHeight: '100px' }}
                    />
                </div>

                <button
                    type="submit"
                    disabled={isLoading}
                    style={{ padding: '10px 24px', background: '#007bff', color: 'white', border: 'none', borderRadius: '6px', fontSize: '15px' }}
                >
                    {isLoading ? 'Создание...' : 'Создать курс'}
                </button>
            </form>
        </div>
    );
};

export default CreateCoursePage;
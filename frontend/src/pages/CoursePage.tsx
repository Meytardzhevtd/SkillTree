import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { getCourseById, createModule, deleteModule, getModulesByCourseId, getMyRoleInCourse, getMyTakenCourses } from '../services/courseApi';

interface Module {
    moduleId: number;
    name: string;
    isOpen: boolean;
}

interface ModuleProgress {
    moduleId: number;
    moduleName: string;
    progress: number;
}

const CoursePage: React.FC = () => {
    const { courseId } = useParams<{ courseId: string }>();
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const [courseName, setCourseName] = useState('');
    const [courseDescription, setCourseDescription] = useState('');
    const [modules, setModules] = useState<Module[]>([]);
    const [newModuleName, setNewModuleName] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [creating, setCreating] = useState(false);
    const [isAdmin, setIsAdmin] = useState(false);

    const [courseProgress, setCourseProgress] = useState<number | null>(null);
    const [moduleProgresses, setModuleProgresses] = useState<ModuleProgress[]>([]);

    useEffect(() => {
        loadCourseAndModules();
    }, [courseId]);

    const loadCourseAndModules = async () => {
        try {
            setLoading(true);
            setError('');

            const [course, modulesData, role] = await Promise.all([
                getCourseById(Number(courseId)),
                getModulesByCourseId(Number(courseId)),
                getMyRoleInCourse(Number(courseId)),
            ]);

            setCourseName(course.name);
            setCourseDescription(course.description)
            setModules(modulesData || []);
            setIsAdmin(role === 'admin');

            if (role === 'student') {
                const takenCourses = await getMyTakenCourses();
                const tc = takenCourses.find((t: any) => t.courseId === Number(courseId));
                if (tc) {
                    setCourseProgress(tc.progress);
                    setModuleProgresses(tc.moduleProgresses || []);
                }
            }
        } catch (err: any) {
            console.error(err);
            setError('Ошибка загрузки курса');
        } finally {
            setLoading(false);
        }
    };

    const getModuleProgress = (moduleId: number): number | null => {
        const mp = moduleProgresses.find(m => m.moduleId === moduleId);
        return mp ? mp.progress : null;
    };

    const handleCreateModule = async () => {
        if (!newModuleName.trim()) { alert('Введите название модуля'); return; }
        try {
            setCreating(true);
            await createModule(Number(courseId), newModuleName, false);
            setNewModuleName('');
            await loadCourseAndModules();
        } catch (err: any) {
            alert(err.response?.data || 'Ошибка создания модуля');
        } finally {
            setCreating(false);
        }
    };

    const handleDeleteModule = async (moduleId: number) => {
        if (!confirm('Удалить модуль? Все задачи внутри модуля также будут удалены.')) return;
        try {
            await deleteModule(moduleId);
            await loadCourseAndModules();
        } catch (err: any) {
            alert(err.response?.data || 'Ошибка удаления модуля');
        }
    };

    const handleModuleClick = (moduleId: number) => {
        const params = new URLSearchParams();
        params.set('courseId', courseId!);
        const takenCourseId = searchParams.get('takenCourseId');
        if (takenCourseId) params.set('takenCourseId', takenCourseId);
        navigate(`/module/${moduleId}?${params.toString()}`);
    };

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;

    if (error) {
        return (
            <div style={{ padding: '20px' }}>
                <p style={{ color: 'red' }}>{error}</p>
                <button onClick={() => navigate('/my-courses')}>Назад к курсам</button>
            </div>
        );
    }

    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
            <button onClick={() => navigate('/my-courses')} style={{ marginBottom: '16px' }}>
                ← Назад к курсам
            </button>

            <h1>{courseName}</h1>
            {courseDescription && (
                <p style={{ color: '#555', marginBottom: '20px', lineHeight: '1.6' }}>
                    {courseDescription}
                </p>
            )}

            {!isAdmin && courseProgress !== null && (
                <div style={{ marginBottom: '24px', padding: '16px', background: '#f8f9fa', borderRadius: '8px', border: '1px solid #e9ecef' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                        <span style={{ fontSize: '14px', fontWeight: 'bold', color: '#333' }}>Прогресс курса</span>
                        <span style={{ fontSize: '14px', color: '#555' }}>{courseProgress.toFixed(0)}%</span>
                    </div>
                    <div style={{ background: '#e9ecef', borderRadius: '4px', height: '10px', overflow: 'hidden' }}>
                        <div style={{
                            background: courseProgress === 100 ? '#28a745' : '#007bff',
                            height: '100%',
                            width: `${courseProgress}%`,
                            transition: 'width 0.3s'
                        }} />
                    </div>
                </div>
            )}

            {isAdmin && (
                <div style={{ marginBottom: '24px', padding: '16px', border: '1px solid #ddd', borderRadius: '8px' }}>
                    <h3 style={{ margin: '0 0 8px 0' }}>Создать новый модуль</h3>
                    <div style={{ display: 'flex', gap: '8px' }}>
                        <input
                            type="text"
                            placeholder="Название модуля"
                            value={newModuleName}
                            onChange={(e) => setNewModuleName(e.target.value)}
                            style={{ flex: 1, padding: '8px' }}
                            disabled={creating}
                        />
                        <button onClick={handleCreateModule} disabled={creating}>
                            {creating ? 'Создание...' : '+ Добавить модуль'}
                        </button>
                    </div>
                </div>
            )}

            <h2>Модули курса</h2>

            {modules.length === 0 ? (
                <p style={{ color: '#888' }}>
                    {isAdmin ? 'В курсе пока нет модулей. Создайте первый модуль!' : 'В курсе пока нет модулей.'}
                </p>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {modules.map((module) => {
                        const progress = getModuleProgress(module.moduleId);
                        return (
                            <div
                                key={module.moduleId}
                                style={{
                                    border: '1px solid #ddd',
                                    borderRadius: '8px',
                                    padding: '16px',
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    cursor: 'pointer',
                                    transition: 'box-shadow 0.2s',
                                }}
                                onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)'}
                                onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'none'}
                            >
                                <div onClick={() => handleModuleClick(module.moduleId)} style={{ flex: 1 }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                        <strong style={{ fontSize: '16px' }}>{module.name}</strong>
                                        {!module.isOpen && <span style={{ color: '#888' }}>🔒</span>}
                                        {!isAdmin && progress === 100 && (
                                            <span style={{ fontSize: '12px', background: '#28a745', color: 'white', padding: '2px 8px', borderRadius: '4px' }}>
                                                ✓ Завершён
                                            </span>
                                        )}
                                    </div>
                                    {!isAdmin && progress !== null && (
                                        <div style={{ marginTop: '8px' }}>
                                            <div style={{ background: '#e9ecef', borderRadius: '4px', height: '6px', overflow: 'hidden' }}>
                                                <div style={{
                                                    background: progress === 100 ? '#28a745' : '#007bff',
                                                    height: '100%',
                                                    width: `${progress}%`,
                                                    transition: 'width 0.3s'
                                                }} />
                                            </div>
                                            <span style={{ fontSize: '12px', color: '#888', marginTop: '2px', display: 'block' }}>
                                                {progress.toFixed(0)}%
                                            </span>
                                        </div>
                                    )}
                                </div>

                                {isAdmin && (
                                    <button
                                        onClick={(e) => { e.stopPropagation(); handleDeleteModule(module.moduleId); }}
                                        style={{ background: '#dc3545', color: 'white', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer', marginLeft: '12px' }}
                                        onMouseEnter={(e) => e.currentTarget.style.background = '#c82333'}
                                        onMouseLeave={(e) => e.currentTarget.style.background = '#dc3545'}
                                    >
                                        Удалить
                                    </button>
                                )}
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
};

export default CoursePage;

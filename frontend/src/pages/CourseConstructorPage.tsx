import React, { useEffect, useState, useMemo, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    getCourseById,
    createModule,
    deleteModule,
    getModulesByCourseId,
    getMyRoleInCourse,
    getAllCourseDependencies,
    createDependency,
    deleteDependency,
    saveModulePositions,
} from '../services/courseApi';
import DependencyGraph from '../components/DependencyGraph';

interface Module {
    moduleId: number;
    name: string;
    isOpen: boolean;
    x?: number | null;
    y?: number | null;
}

interface Dependency {
    id: number;
    blockerId: number;
    blockerName: string;
    dependentId: number;
    dependentName: string;
}

const CourseConstructorPage: React.FC = () => {
    const { courseId } = useParams<{ courseId: string }>();
    const navigate = useNavigate();

    const [courseName, setCourseName] = useState('');
    const [courseDescription, setCourseDescription] = useState('');
    const [modules, setModules] = useState<Module[]>([]);
    const [newModuleName, setNewModuleName] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [creating, setCreating] = useState(false);

    // Состояние для зависимостей
    const [dependencies, setDependencies] = useState<Dependency[]>([]);
    const [loadingDeps, setLoadingDeps] = useState(false);

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
            setCourseDescription(course.description);
            setModules((modulesData || []).map((m: any) => ({
                ...m,
                positionX: m.x,
                positionY: m.y,
            })));

            if (role === 'admin') {
                await loadDependencies(modulesData || []);
            } else {
                navigate(`/course/${courseId}`);
            }
        } catch (err: any) {
            console.error(err);
            setError('Ошибка загрузки курса');
        } finally {
            setLoading(false);
        }
    };

    const loadDependencies = async (modulesList: Module[]) => {
        setLoadingDeps(true);
        try {
            const graph = await getAllCourseDependencies(Number(courseId));
            console.log('Raw graph data:', graph);

            const flatDeps: Dependency[] = [];
            for (const [blockerId, blockedList] of Object.entries(graph)) {
                const blockerMod = modulesList.find(m => m.moduleId === Number(blockerId));
                if (!blockerMod) continue;
                for (const item of blockedList as any[]) {
                    flatDeps.push({
                        id: item.id,
                        blockerId: Number(blockerId),
                        blockerName: blockerMod.name,
                        dependentId: item.blockedModuleId,
                        dependentName: item.blockedModuleName,
                    });
                }
            }
            setDependencies(flatDeps);
        } catch (err) {
            console.error('Ошибка загрузки зависимостей', err);
        } finally {
            setLoadingDeps(false);
        }
    };

    const handleCreateModule = async () => {
        if (!newModuleName.trim()) {
            alert('Введите название модуля');
            return;
        }
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
        if (!confirm('Удалить модуль? Все задачи и зависимости тоже удалятся.')) return;
        try {
            await deleteModule(moduleId);
            await loadCourseAndModules();
        } catch (err: any) {
            alert(err.response?.data || 'Ошибка удаления модуля');
        }
    };

    const handleModuleClick = (moduleId: number) => {
        navigate(`/module/${moduleId}?courseId=${courseId}`);
    };

    const handleGraphNodeClick = (nodeId: number) => {
        navigate(`/module/${nodeId}?courseId=${courseId}`);
    };

    const onNodeDragStop = useCallback(async (nodeId: number, position: { x: number; y: number }) => {
        try {
            await saveModulePositions(nodeId, position.x, position.y);
            setModules(prev => prev.map(m =>
                m.moduleId === nodeId ? { ...m, x: position.x, y: position.y } : m
            ));
        } catch (err) {
            console.error('Ошибка сохранения позиции:', err);
            alert('Не удалось сохранить позицию модуля');
        }
    }, []);

    // Обработчик создания зависимости через граф
    const handleGraphConnect = useCallback(async ({ source, target }: { source: number; target: number }) => {
        try {
            const success = await createDependency(source, target);
            if (success) {
                await loadDependencies(modules);
                return true;
            }
            return false;
        } catch (err) {
            console.error(err);
            return false;
        }
    }, [modules]);

    // Обработчик удаления зависимости через граф
    const handleGraphEdgeClick = useCallback(async (edgeId: string, dependencyId: number) => {
        try {
            await deleteDependency(dependencyId);
            await loadDependencies(modules);
        } catch (err) {
            console.error(err);
            alert('Ошибка удаления зависимости');
        }
    }, [modules]);

    const graphModules = useMemo(() => modules
            .filter(m => m && m.moduleId)
            .map(m => ({
                id: m.moduleId,
                name: m.name,
                x: m.x ?? undefined,
                y: m.y ?? undefined,
            })),
        [modules]
    );

    const graphEdges = useMemo(() => dependencies
            .filter(d => d && d.id && d.blockerId && d.dependentId)
            .map(d => ({ id: d.id, from: d.blockerId, to: d.dependentId })),
        [dependencies]
    );

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
        <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
            <button onClick={() => navigate('/my-courses')} style={{ marginBottom: '16px' }}>
                ← Назад к курсам
            </button>

            <h1>{courseName}</h1>
            {courseDescription && (
                <p style={{ color: '#555', marginBottom: '20px', lineHeight: '1.6' }}>
                    {courseDescription}
                </p>
            )}

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

            <h2>Модули курса</h2>
            {modules.length === 0 ? (
                <p>В курсе пока нет модулей. Создайте первый модуль!</p>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {modules.map(module => (
                        <div
                            key={module.moduleId}
                            style={{
                                border: '1px solid #ddd',
                                borderRadius: '8px',
                                padding: '16px',
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                transition: 'box-shadow 0.2s',
                                cursor: 'pointer',
                            }}
                            onMouseEnter={(e) => (e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)')}
                            onMouseLeave={(e) => (e.currentTarget.style.boxShadow = 'none')}
                            onClick={() => handleModuleClick(module.moduleId)}
                        >
                            <div style={{ flex: 1 }}>
                                <strong>{module.name}</strong>
                            </div>
                            <button
                                onClick={(e) => { e.stopPropagation(); handleDeleteModule(module.moduleId); }}
                                style={{
                                    background: '#dc3545',
                                    color: 'white',
                                    border: 'none',
                                    padding: '6px 12px',
                                    borderRadius: '4px',
                                    cursor: 'pointer',
                                    marginLeft: '12px',
                                }}
                            >
                                Удалить
                            </button>
                        </div>
                    ))}
                </div>
            )}

            {/* Граф зависимостей */}
            <div style={{ marginTop: '32px', borderTop: '2px solid #ddd', paddingTop: '20px' }}>
                <h2>Граф зависимостей модулей</h2>
                {loadingDeps && <p>Загрузка зависимостей...</p>}
                {!loadingDeps && modules.length > 0 && (
                    <DependencyGraph
                        modules={graphModules}
                        dependencies={graphEdges}
                        onNodeClick={handleGraphNodeClick}
                        onNodeDragStop={onNodeDragStop}
                        onConnect={handleGraphConnect}
                        onEdgeClick={handleGraphEdgeClick}
                        readOnly={false}
                    />
                )}
                {!loadingDeps && modules.length === 0 && <p>Нет модулей для отображения графа</p>}
                <button onClick={() => loadDependencies(modules)} style={{ marginTop: '16px' }}>
                    Обновить данные
                </button>
            </div>
        </div>
    );
};

export default CourseConstructorPage;
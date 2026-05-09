import React, { useEffect, useMemo } from 'react';
import ReactFlow, {
    Background,
    Controls,
    MiniMap,
    useNodesState,
    useEdgesState,
    MarkerType,
} from 'reactflow';
import type { Node, Connection, Edge } from 'reactflow';
import 'reactflow/dist/style.css';

interface ModuleNode {
    id: number;
    name: string;
    x?: number;
    y?: number;
}

interface DependencyEdge {
    id: number;
    from: number;
    to: number;
}

interface DependencyGraphProps {
    modules: ModuleNode[];
    dependencies: DependencyEdge[];
    onNodeClick?: (nodeId: number) => void;
    onNodeDragStop?: (nodeId: number, position: { x: number; y: number }) => void;
    readOnly?: boolean;
    getNodeStyle?: (nodeId: number) => React.CSSProperties;
    onConnect?: (params: { source: number; target: number }) => Promise<boolean>;
    onEdgeClick?: (edgeId: string, dependencyId: number) => void;
}

const DependencyGraph: React.FC<DependencyGraphProps> = ({
                                                             modules,
                                                             dependencies,
                                                             onNodeClick,
                                                             onNodeDragStop,
                                                             readOnly = false,
                                                             getNodeStyle,
                                                             onConnect,
                                                             onEdgeClick,
                                                         }) => {
    const safeModules = Array.isArray(modules) ? modules : [];
    const safeDependencies = Array.isArray(dependencies) ? dependencies : [];

    const initialNodes = useMemo(() => {
        return safeModules
            .filter(mod => mod && mod.id && mod.name)
            .map((mod, index) => {
                const baseStyle = {
                    background: '#f0f9ff',
                    border: '1px solid #007bff',
                    borderRadius: '8px',
                    padding: '10px',
                    width: 180,
                };
                const customStyle = getNodeStyle ? getNodeStyle(mod.id) : {};
                const x = mod.x !== undefined ? mod.x : (index % 3) * 250;
                const y = mod.y !== undefined ? mod.y : Math.floor(index / 3) * 150;
                return {
                    id: mod.id.toString(),
                    data: { label: mod.name },
                    position: { x, y },
                    style: { ...baseStyle, ...customStyle },
                    draggable: !readOnly,
                };
            });
    }, [safeModules, getNodeStyle, readOnly]);

    const initialEdges = useMemo(() => {
        return safeDependencies
            .filter(dep => dep && dep.id && dep.from && dep.to)
            .map((dep) => ({
                id: `edge-${dep.id}`,
                source: dep.from.toString(),
                target: dep.to.toString(),
                type: 'smoothstep',
                markerEnd: { type: MarkerType.ArrowClosed },
                data: { dependencyId: dep.id },
            }));
    }, [safeDependencies]);

    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

    useEffect(() => {
        setNodes(initialNodes);
    }, [initialNodes, setNodes]);

    useEffect(() => {
        setEdges(initialEdges);
    }, [initialEdges, setEdges]);

    const handleNodeClick = (_event: React.MouseEvent, node: Node) => {
        if (onNodeClick) {
            onNodeClick(Number(node.id));
        }
    };

    const handleNodeDragStop = (_event: React.MouseEvent, node: Node) => {
        if (onNodeDragStop && !readOnly) {
            onNodeDragStop(Number(node.id), node.position);
        }
    };

    const handleConnect = async (connection: Connection) => {
        if (!connection.source || !connection.target) return;
        const sourceId = Number(connection.source);
        const targetId = Number(connection.target);
        if (sourceId === targetId) {
            alert('Нельзя создать зависимость от модуля к самому себе');
            return;
        }
        if (onConnect) {
            const success = await onConnect({ source: sourceId, target: targetId });
            if (!success) alert('Не удалось создать зависимость (цикл или уже существует)');
        }
    };

    const handleEdgeClick = (_event: React.MouseEvent, edge: Edge) => {
        if (readOnly) return;
        const dependencyId = edge.data?.dependencyId;
        if (dependencyId && onEdgeClick) {
            if (confirm('Удалить зависимость?')) {
                onEdgeClick(edge.id, dependencyId);
            }
        }
    };

    if (initialNodes.length === 0) {
        return <div style={{ padding: '20px', textAlign: 'center', color: '#888' }}>Нет модулей для отображения графа</div>;
    }

    return (
        <div style={{ width: '100%', height: '500px', border: '1px solid #ddd', borderRadius: '8px' }}>
            <ReactFlow
                nodes={nodes}
                edges={edges}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onNodeClick={handleNodeClick}
                onNodeDragStop={handleNodeDragStop}
                onConnect={handleConnect}
                onEdgeClick={handleEdgeClick}
                nodesDraggable={!readOnly}
                fitView
                attributionPosition="bottom-right"
            >
                <Background />
                <Controls />
                <MiniMap />
            </ReactFlow>
        </div>
    );
};

export default DependencyGraph;
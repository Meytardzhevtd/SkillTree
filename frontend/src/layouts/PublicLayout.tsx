import { Outlet } from 'react-router-dom'

function PublicLayout() {
    return (
        <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
            <header style={{ borderBottom: '1px solid #ddd', padding: '12px 16px' }}>
                <strong>SkillTree</strong>
            </header>

            <main style={{ flex: 1 }}>
                <Outlet />
            </main>

            <footer style={{ borderTop: '1px solid #ddd', padding: '12px 16px' }}>
                © SkillTree
            </footer>
        </div>
    )
}

export default PublicLayout
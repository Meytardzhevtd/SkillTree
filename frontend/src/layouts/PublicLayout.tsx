import { Outlet } from 'react-router-dom'

function PublicLayout() {
    return (
        <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
            <header style={{ borderBottom: '1px solid #ddd', padding: '12px 16px' }}>
                <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '0 20px' }}>
                    <strong>SkillTree</strong>
                </div>
            </header>

            <main style={{ flex: 1 }}>
                <div className="content-container">
                    <Outlet />
                </div>
            </main>

            <footer style={{ borderTop: '1px solid #ddd', padding: '12px 16px', textAlign: 'center' }}>
                <div style={{ maxWidth: '1280px', margin: '0 auto' }}>
                    © SkillTree
                </div>
            </footer>
        </div>
    )
}

export default PublicLayout
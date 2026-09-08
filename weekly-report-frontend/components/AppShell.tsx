'use client';

import { usePathname } from 'next/navigation';
import Link from 'next/link';
import { signOut, useSession } from 'next-auth/react';

const TEAM_MEMBER_LINKS = [
  { href: '/my-reports', label: 'My reports' },
];

const MANAGER_LINKS = [
  { href: '/dashboard', label: 'Dashboard' },
  { href: '/my-reports', label: 'Team reports' },
  { href: '/projects', label: 'Projects' },
  { href: '/users', label: 'Users' },
];

export default function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { data: session } = useSession();

  const links = session?.isManager ? MANAGER_LINKS : TEAM_MEMBER_LINKS;

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">Weekly Reports</div>

        <nav className="sidebar-nav">
          {links.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className={`sidebar-link ${pathname?.startsWith(link.href) ? 'active' : ''}`}
            >
              {link.label}
            </Link>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="sidebar-user">
            <div>
              <div className="sidebar-user-name">{session?.user?.name || session?.user?.email}</div>
              <div className="sidebar-user-role">{session?.isManager ? 'Manager' : 'Team member'}</div>
            </div>
          </div>
          <button className="btn btn-sm" style={{ width: '100%', marginTop: 8 }} onClick={() => signOut({ callbackUrl: '/login' })}>
            Sign out
          </button>
        </div>
      </aside>

      <main className="main-content">{children}</main>
    </div>
  );
}

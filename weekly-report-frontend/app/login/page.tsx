'use client';

import { useEffect } from 'react';
import { signIn, useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';

export default function LoginPage() {
  const { data: session, status } = useSession();
  const router = useRouter();

  useEffect(() => {
    if (status === 'authenticated') {
      router.replace(session?.isManager ? '/dashboard' : '/my-reports');
    }
  }, [status, session, router]);

  return (
    <div className="login-screen">
      <div className="login-card">
        <h1>Weekly Reports</h1>
        <p>Sign in with your workspace account to continue.</p>
        <button className="btn btn-primary" style={{ width: '100%', justifyContent: 'center' }} onClick={() => signIn('keycloak')}>
          Sign in
        </button>
      </div>
    </div>
  );
}

import { withAuth } from 'next-auth/middleware';
import { NextResponse } from 'next/server';

const MANAGER_ONLY_PATHS = ['/dashboard', '/projects', '/users', '/review'];

export default withAuth(
  function middleware(req) {
    const { pathname } = req.nextUrl;
    const isManager = (req.nextauth.token?.roles as string[] | undefined)?.includes('MANAGER');

    if (MANAGER_ONLY_PATHS.some((p) => pathname.startsWith(p)) && !isManager) {
      return NextResponse.redirect(new URL('/my-reports', req.url));
    }
    return NextResponse.next();
  },
  {
    callbacks: {
      authorized: ({ token }) => !!token,
    },
    pages: { signIn: '/login' },
  }
);

export const config = {
  matcher: [
    '/dashboard/:path*',
    '/my-reports/:path*',
    '/projects/:path*',
    '/users/:path*',
    '/review/:path*',
    '/reports/:path*',
  ],
};

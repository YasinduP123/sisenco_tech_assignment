import 'next-auth';

declare module 'next-auth' {
  interface Session {
    accessToken?: string;
    roles?: string[];
    isManager?: boolean;
  }
}

declare module 'next-auth/jwt' {
  interface JWT {
    accessToken?: string;
    refreshToken?: string;
    roles?: string[];
  }
}

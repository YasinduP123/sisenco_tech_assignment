import NextAuth, { type NextAuthOptions } from 'next-auth';
import KeycloakProvider from 'next-auth/providers/keycloak';

export const authOptions: NextAuthOptions = {
  providers: [
    KeycloakProvider({
      clientId: process.env.KEYCLOAK_CLIENT_ID as string,
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET as string,
      issuer: process.env.KEYCLOAK_ISSUER as string,
    }),
  ],

  callbacks: {
    async jwt({ token, account }) {
      if (account) {
        token.accessToken = account.access_token;
        token.refreshToken = account.refresh_token;

        try {
          const payload = JSON.parse(
            Buffer.from((account.access_token as string).split('.')[1], 'base64').toString()
          );
          token.roles = payload.realm_access?.roles || [];
        } catch {
          token.roles = [];
        }
      }
      return token;
    },

    async session({ session, token }) {
      session.accessToken = token.accessToken as string;
      session.roles = (token.roles as string[]) || [];
      session.isManager = session.roles.includes('MANAGER');
      return session;
    },
  },

  session: { strategy: 'jwt' },
  pages: { signIn: '/login' },
};

const handler = NextAuth(authOptions);
export { handler as GET, handler as POST };

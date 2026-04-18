

export async function getCurrentUser() {
  let userId = 0;
  let userEmail = 'example@example.com';
  let user;
  if (process.env.NEXT_PUBLIC_CLERK_ENABLED) {
  }
  return {
    userId,
    userEmail,
    user,
    isAdmin: true,
  }
}

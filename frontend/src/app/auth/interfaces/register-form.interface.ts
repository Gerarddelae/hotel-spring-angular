export interface RegisterForm {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  role: 'USER' | 'ADMIN';
}

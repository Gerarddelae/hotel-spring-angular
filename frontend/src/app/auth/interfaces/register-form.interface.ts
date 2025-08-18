export interface RegisterForm {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  name: string;        // Nombre del hotel
  address: string;
  city: string;
  country: string;
  phone: string;
  hotelEmail: string;  // Email del hotel
  description?: string;
}


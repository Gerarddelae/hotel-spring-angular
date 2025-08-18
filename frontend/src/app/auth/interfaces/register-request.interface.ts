export interface RegisterRequest {
  user: {
    username: string;
    password: string;
    email: string;
  };
  hotel: {
    name: string;
    address: string;
    city: string;
    country: string;
    phone: string;
    email: string;
    description?: string; // opcional
  };
}


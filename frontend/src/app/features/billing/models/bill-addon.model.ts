export interface BillAddon {
  id?: number;
  addonId?: number;
  addonName?: string;
  name?: string;
  description?: string;
  // El backend usa unitPrice
  unitPrice?: number;
  price?: number;
  quantity?: number;
  // El backend usa totalPrice
  totalPrice?: number;
  subtotal?: number;
  total?: number;
  // Objeto addon embebido que puede venir del backend
  addon?: {
    id: number;
    name: string;
    price: number;
  };
}

import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AddonsService } from './addons.service';
import { AddonRequest } from './models/addon-request.interface';
import { AddonResponse } from './models/addon-response.interface';

describe('AddonsService', () => {
  let service: AddonsService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://127.0.0.1:8080/addons';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AddonsService]
    });
    service = TestBed.inject(AddonsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should load addons on initialization', () => {
    const mockAddons: AddonResponse[] = [
      { id: 1, name: 'Spa', description: 'Spa service', price: 50, createdAt: '2025-01-01', quantity: 1, subtotal: 50 }
    ];

    // Initial load from constructor
    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockAddons);

    service.addons$.subscribe(addons => {
      expect(addons).toEqual(mockAddons);
    });
  });

  it('should get addon by id', () => {
    const mockAddon: AddonResponse = { id: 1, name: 'Spa', description: 'Spa service', price: 50, createdAt: '2025-01-01', quantity: 1, subtotal: 50 };

    service.get(1).subscribe(addon => {
      expect(addon).toEqual(mockAddon);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockAddon);
  });

  it('should create addon', () => {
    const newAddon: AddonRequest = { name: 'Gym', description: 'Gym access', price: 30 };
    const createdAddon: AddonResponse = { id: 2, ...newAddon, createdAt: '2025-01-02', quantity: 1, subtotal: 30 };

    service.create(newAddon).subscribe(addon => {
      expect(addon).toEqual(createdAddon);
    });

    const createReq = httpMock.expectOne(apiUrl);
    expect(createReq.request.method).toBe('POST');
    expect(createReq.request.body).toEqual(newAddon);
    createReq.flush(createdAddon);

    // loadAddons is called after create
    const loadReq = httpMock.expectOne(apiUrl);
    loadReq.flush([createdAddon]);
  });

  it('should update addon', () => {
    const updateData: AddonRequest = { name: 'Spa Updated', description: 'Updated description', price: 60 };
    const updatedAddon: AddonResponse = { id: 1, ...updateData, createdAt: '2025-01-01', quantity: 1, subtotal: 60 };

    service.update(1, updateData).subscribe(addon => {
      expect(addon).toEqual(updatedAddon);
    });

    const updateReq = httpMock.expectOne(`${apiUrl}/1`);
    expect(updateReq.request.method).toBe('PUT');
    expect(updateReq.request.body).toEqual(updateData);
    updateReq.flush(updatedAddon);

    // loadAddons is called after update
    const loadReq = httpMock.expectOne(apiUrl);
    loadReq.flush([updatedAddon]);
  });

  it('should delete addon', () => {
    service.delete(1).subscribe();

    const deleteReq = httpMock.expectOne(`${apiUrl}/1`);
    expect(deleteReq.request.method).toBe('DELETE');
    deleteReq.flush(null);

    // loadAddons is called after delete
    const loadReq = httpMock.expectOne(apiUrl);
    loadReq.flush([]);
  });

  it('should search addons by name', () => {
    const searchResults: AddonResponse[] = [
      { id: 1, name: 'Spa', description: 'Spa service', price: 50, createdAt: '2025-01-01', quantity: 1, subtotal: 50 }
    ];

    service.search('Spa').subscribe(addons => {
      expect(addons).toEqual(searchResults);
    });

    const searchReq = httpMock.expectOne(req => req.url === `${apiUrl}/search` && req.params.get('name') === 'Spa');
    expect(searchReq.request.method).toBe('GET');
    searchReq.flush(searchResults);
  });
});

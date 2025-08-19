import { TestBed } from '@angular/core/testing';
import { CountryService } from './country.service';
import { provideHttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('CountryService', () => {
  let service: CountryService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule], // ✅ sigue siendo válido
      providers: [CountryService],
    });
    service = TestBed.inject(CountryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // ✅ verifica que no haya requests pendientes
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BillService } from './bill.service';

describe('BillService', () => {
  let service: BillService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BillService]
    });
    service = TestBed.inject(BillService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { AddonsComponent } from './addons.component';
import { AddonsService } from './addons.service';
import { of } from 'rxjs';

describe('AddonsComponent', () => {
  let component: AddonsComponent;
  let fixture: ComponentFixture<AddonsComponent>;
  let addonsService: jasmine.SpyObj<AddonsService>;

  beforeEach(async () => {
    const addonsServiceSpy = jasmine.createSpyObj('AddonsService', [
      'loadAddons',
      'search',
      'create',
      'update',
      'delete'
    ], {
      addons$: of([])
    });

    await TestBed.configureTestingModule({
      imports: [
        AddonsComponent,
        HttpClientTestingModule,
        MatDialogModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AddonsService, useValue: addonsServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AddonsComponent);
    component = fixture.componentInstance;
    addonsService = TestBed.inject(AddonsService) as jasmine.SpyObj<AddonsService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have correct columns configuration', () => {
    expect(component.columns).toEqual(['id', 'name', 'description', 'price']);
  });

  it('should call loadAddons on refresh', () => {
    component.refresh();
    expect(addonsService.loadAddons).toHaveBeenCalled();
  });

  it('should call search when searchTerm is not empty', () => {
    component.searchTerm = 'Spa';
    component.onSearch();
    expect(addonsService.search).toHaveBeenCalledWith('Spa');
  });

  it('should call refresh when searchTerm is empty', () => {
    component.searchTerm = '';
    spyOn(component, 'refresh');
    component.onSearch();
    expect(component.refresh).toHaveBeenCalled();
  });

  it('should call delete and refresh on onDelete', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    addonsService.delete.and.returnValue(of(undefined));
    spyOn(component, 'refresh');

    component.onDelete(1);

    expect(addonsService.delete).toHaveBeenCalledWith(1);
    expect(component.refresh).toHaveBeenCalled();
  });

  it('should not delete if user cancels', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.onDelete(1);
    expect(addonsService.delete).not.toHaveBeenCalled();
  });
});

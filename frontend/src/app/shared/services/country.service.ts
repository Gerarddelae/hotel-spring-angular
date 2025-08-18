import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Country {
  name: string;
  code: string;
}

@Injectable({
  providedIn: 'root'
})
export class CountryService {

  private readonly apiUrl = 'https://restcountries.com/v3.1/all';

  constructor(private http: HttpClient) {}

  // Devuelve lista de países ordenados por nombre
  getCountries(): Observable<Country[]> {
    const fields = 'name,cca2'; // Especifica los campos que deseas recibir

    return this.http.get<any[]>(`${this.apiUrl}?fields=${fields}`).pipe(
      map(countries =>
        countries
          .map(c => ({
            name: c.name.common,
            code: c.cca2 // código ISO alfa-2
          }))
          .sort((a, b) => a.name.localeCompare(b.name))
      )
    );
  }
}

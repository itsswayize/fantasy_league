import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, switchMap, delay } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LeagueService {
  // Use your actual Render ID
  private apiUrl = 'https://fpl-backend-em6n.onrender.com/api/league';

  constructor(private http: HttpClient) { }

  // ======================================================
  // 1. UPDATED FIXTURES METHOD (Fixes the Error)
  // ======================================================
  getFixtures(from?: string, to?: string): Observable<any[]> {
    let params = new HttpParams()
      .set('t', new Date().getTime().toString()); // Cache busting
    
    // If dates are provided, send them to the backend
    if (from && to) {
      params = params.set('from', from).set('to', to);
    }

    return this.http.get<any[]>(`${this.apiUrl}/fixtures`, { params });
  }

  // ======================================================
  // 2. OTHER METHODS
  // ======================================================
  getStandings(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/standings`);
  }

  getClubDetails(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/clubs/${id}`);
  }

  getClubFixtures(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/clubs/${id}/fixtures`);
  }

  // Sync and Action Methods
  syncTeams(): Observable<string> {
    return this.http.post(`${this.apiUrl}/sync-teams`, {}, { responseType: 'text' });
  }

  generateFixtures(): Observable<any> {
    return this.http.post(`${this.apiUrl}/generate-fixtures`, {});
  }

  simulateMatches(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/simulate`, {});
  }

  syncInjuries(): Observable<any> {
    return this.http.post(`${this.apiUrl}/sync-injuries`, {});
  }

  // Chained Auto-Sync Methods
  getFixturesWithSync(): Observable<any[]> {
    return this.generateFixtures().pipe(
      delay(500),
      switchMap(() => this.getFixtures())
    );
  }

  getStandingsWithSync(): Observable<any[]> {
    return this.http.post(`${this.apiUrl}/sync-standings`, {}).pipe(
      delay(800),
      switchMap(() => this.getStandings())
    );
  }

  getClubsWithSync(): Observable<any[]> {
    return this.syncTeams().pipe(
      delay(800),
      switchMap(() => this.http.get<any[]>(`${this.apiUrl}/clubs`))
    );
  }

  syncTopScorers(): Observable<any> {
    return this.http.post(`${this.apiUrl}/sync-topscorers`, {});
  }

  getStatisticsWithSync(): Observable<any[]> {
    return this.syncTopScorers().pipe(
      delay(800),
      switchMap(() => this.getStandings())
    );
  }
}
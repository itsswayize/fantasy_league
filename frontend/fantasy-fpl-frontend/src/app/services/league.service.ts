import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http'; // Added HttpParams
import { Observable, switchMap, delay } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LeagueService {
  // Fixed: Using your actual Render ID 'em6n'
  private apiUrl = 'https://fpl-backend-em6n.onrender.com/api/league';

  constructor(private http: HttpClient) { }

  // Basic Data Getters
  getStandings(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/standings`);
  }

  getFixtures(): Observable<any[]> {
    // Adding ?t= ensures you get fresh data from the server, not a cached version
    const timestamp = new Date().getTime();
    return this.http.get<any[]>(`${this.apiUrl}/fixtures?t=${timestamp}`);
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

  // FIXED: Standardized way to send query parameters
  getFixturesByDate(from: string, to: string): Observable<any> {
  const params = new HttpParams()
    .set('from', from)
    .set('to', to)
    .set('t', new Date().getTime().toString()); // Cache busting for sync calls
    
  return this.http.get(`${this.apiUrl}/fixtures/sync`, { params });
}
}
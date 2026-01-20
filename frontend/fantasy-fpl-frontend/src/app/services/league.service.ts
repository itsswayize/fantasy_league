import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap, delay } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LeagueService {
  private apiUrl = 'https://fpl-backend-xxxx.onrender.com/api/league';

  constructor(private http: HttpClient) { }

  // Basic Data Getters
  getStandings(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/standings`);
  }

  getFixtures(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/fixtures`);
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

  // Chained Auto-Sync Methods for your Tabs
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
      switchMap(() => this.getStandings()) // Fetching teams includes the squad with updated stats
    );
}

// Add this method inside your LeagueService class
getFixturesByDate(from: string, to: string): Observable<any> {
  return this.http.get(`${this.apiUrl}/fixtures/sync?from=${from}&to=${to}`);
}

}
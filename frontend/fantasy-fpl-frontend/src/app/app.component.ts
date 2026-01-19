import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { LeagueService } from './services/league.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'fantasy-fpl-frontend';
  standings: any[] = [];
  fixtures: any[] = [];

  constructor(private leagueService: LeagueService) {}

  // ADDED: This method resolves the TypeScript error
  ngOnInit(): void {
    // Automatically trigger team synchronization on app startup
    this.leagueService.syncTeams().subscribe({
      next: () => {
        console.log('Initial team sync complete');
        this.loadData();
      },
      error: (err: any) => console.error('Startup sync failed', err)
    });
  }

  loadData() {
    this.leagueService.getStandings().subscribe(data => {
      this.standings = data;
    });
    this.leagueService.getFixtures().subscribe(data => {
      this.fixtures = data;
    });
  }

  syncWithApi() {
    // We'll use the HttpClient directly or add this method to your LeagueService
    this.leagueService.syncTeams().subscribe({
  next: (response: string) => {
    alert('Sync successful!');
  },
  error: (err: any) => { // Added :any here
    console.error(err);
  }
});
  }

  generateFixtures() {
    this.leagueService.generateFixtures().subscribe(() => {
      alert('Fixtures Generated!');
      this.loadData();
    });
  }

  simulateDay() {
    this.leagueService.simulateMatches().subscribe(() => {
      alert('Matches Simulated!');
      this.loadData();
    });
  }
}
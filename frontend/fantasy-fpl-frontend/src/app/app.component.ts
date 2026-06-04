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

  constructor(private leagueService: LeagueService) {}

  // Data loading is now handled by individual components to prevent duplicate API calls
  ngOnInit(): void {
  }

  syncWithApi() {
    this.leagueService.syncTeams().subscribe({
      next: (response: string) => {
        alert('Sync successful!');
      },
      error: (err: any) => {
        console.error(err);
      }
    });
  }

  generateFixtures() {
    this.leagueService.generateFixtures().subscribe(() => {
      alert('Fixtures Generated!');
    });
  }

  simulateDay() {
    this.leagueService.simulateMatches().subscribe(() => {
      alert('Matches Simulated!');
      // FIX: Removed this.loadData() since child components manage their own data now.
      // If you want the views to update automatically after simulation, uncomment the line below:
      // window.location.reload(); 
    });
  }
}
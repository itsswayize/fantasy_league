import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LeagueService } from '../../services/league.service';

@Component({
  selector: 'app-matches',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './matches.component.html',
  styleUrl: './matches.component.css'
})
export class MatchesComponent implements OnInit {
  fixtures: any[] = [];
  loading: boolean = false;

  // Navigation Data
  matchweeks = [
    { label: 'Matchweek 1', from: '2025-08-15', to: '2025-08-18' },
    { label: 'Matchweek 2', from: '2025-08-22', to: '2025-08-25' },
    { label: 'Matchweek 3', from: '2025-08-30', to: '2025-08-31' },
    { label: 'Matchweek 4', from: '2025-09-13', to: '2025-09-15' },
    { label: 'Matchweek 5', from: '2025-09-20', to: '2025-09-22' },
    { label: 'Matchweek 6', from: '2025-09-27', to: '2025-09-29' },
    { label: 'Matchweek 7', from: '2025-10-04', to: '2025-10-06' },
    { label: 'Matchweek 8', from: '2025-10-18', to: '2025-10-20' },
    { label: 'Matchweek 9', from: '2025-10-24', to: '2025-10-27' },
    { label: 'Matchweek 10', from: '2025-11-01', to: '2025-11-03' },
    { label: 'Matchweek 11', from: '2025-11-08', to: '2025-11-10' },
    { label: 'Matchweek 12', from: '2025-11-22', to: '2025-11-24' },
    { label: 'Matchweek 13', from: '2025-11-29', to: '2025-12-01' },
    { label: 'Matchweek 14', from: '2025-12-02', to: '2025-12-04' },
    { label: 'Matchweek 15', from: '2025-12-06', to: '2025-12-08' },
    { label: 'Matchweek 16', from: '2025-12-13', to: '2025-12-15' },
    { label: 'Matchweek 17', from: '2025-12-20', to: '2025-12-22' },
    { label: 'Matchweek 18', from: '2025-12-26', to: '2025-12-28' },
    { label: 'Matchweek 19', from: '2025-12-30', to: '2026-01-01' },
    { label: 'Matchweek 20', from: '2026-01-03', to: '2026-01-05' },
    { label: 'Matchweek 21', from: '2026-01-06', to: '2026-01-08' },
    { label: 'Matchweek 22', from: '2026-01-17', to: '2026-01-19' }
  ];
 currentWeekIndex = 21;// Default to Matchweek 22

  constructor(private leagueService: LeagueService) {}

  ngOnInit(): void {
    this.loadCurrentWeek();
  }

  loadCurrentWeek() {
    this.loading = true;
    const week = this.matchweeks[this.currentWeekIndex];

    // 1. Sync from API for this specific date range
    this.leagueService.getFixturesByDate(week.from, week.to).subscribe({
      next: () => {
        // 2. Load the synced data from local DB
        this.loadLocalFixtures();
      },
      error: (err) => {
        console.error('Sync failed', err);
        this.loading = false;
      }
    });
  }

  loadLocalFixtures() {
  this.leagueService.getFixtures().subscribe({
    next: (data) => {
      const week = this.matchweeks[this.currentWeekIndex];
      const start = new Date(week.from).getTime();
      const end = new Date(week.to).getTime();

      // Filter and use a Set or simple map to ensure UI uniqueness
      const uniqueFixtures = data.filter(f => {
        const matchTime = new Date(f.matchDate).getTime();
        return matchTime >= start && matchTime <= end;
      });

      // Clear the current list and replace it to prevent UI stacking
      this.fixtures = [...uniqueFixtures].sort((a, b) => 
        new Date(a.matchDate).getTime() - new Date(b.matchDate).getTime()
      );
      
      this.loading = false;
    }
  });
}

  previousWeek() {
    if (this.currentWeekIndex > 0) {
      this.currentWeekIndex--;
      this.loadCurrentWeek();
    }
  }

  nextWeek() {
    if (this.currentWeekIndex < this.matchweeks.length - 1) {
      this.currentWeekIndex++;
      this.loadCurrentWeek();
    }
  }
}

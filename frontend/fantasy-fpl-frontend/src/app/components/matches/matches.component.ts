import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LeagueService } from '../../services/league.service';
import { interval, Subscription } from 'rxjs';

// Helper interface for grouping
interface DateGroup {
  date: string;
  fixtures: any[];
}

@Component({
  selector: 'app-matches',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './matches.component.html',
  styleUrl: './matches.component.css'
})
export class MatchesComponent implements OnInit, OnDestroy {
  allFixtures: any[] = []; // Store raw data here
  groupedFixtures: DateGroup[] = []; // Store grouped data for the view
  loading: boolean = false;
  currentWeekIndex: number = 0;

  private pollingSubscription?: Subscription;

  // Navigation Data - Matchweeks
  matchweeks = [
    { label: 'Matchweek 1', from: '2025-08-15', to: '2025-08-18' },
    // ... keep your existing matchweeks list ...
    { label: 'Matchweek 21', from: '2026-01-06', to: '2026-01-08' },
    { label: 'Matchweek 22', from: '2026-01-17', to: '2026-01-19' },
    { label: 'Matchweek 23', from: '2026-01-24', to: '2026-01-26' },
    { label: 'Matchweek 24', from: '2026-01-31', to: '2026-02-02' },
    { label: 'Matchweek 25', from: '2026-02-07', to: '2026-02-09' }
    // Add more weeks as needed for the season
  ];

  constructor(private leagueService: LeagueService) {}

  ngOnInit(): void {
    this.determineCurrentWeek(); // 1. Find the active week automatically
    this.loadFixtures(); // 2. Fetch data

    // Refresh every 60s
    this.pollingSubscription = interval(60000).subscribe(() => {
      this.loadFixtures();
    });
  }

  ngOnDestroy(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
  }

  determineCurrentWeek() {
    const today = new Date().getTime();
    
    // Find the first matchweek that ends AFTER today
    const foundIndex = this.matchweeks.findIndex(week => {
      const weekEnd = new Date(week.to).getTime();
      return weekEnd >= today;
    });

    // If found, use it; otherwise default to the last one or 0
    this.currentWeekIndex = foundIndex !== -1 ? foundIndex : 0;
  }

  loadFixtures(): void {
    this.loading = true;
    this.leagueService.getFixtures().subscribe({
      next: (data) => {
        this.allFixtures = data;
        this.filterAndGroupFixtures(); // Process the raw data
        this.loading = false;
      },
      error: (err) => {
        console.error("Error fetching fixtures:", err);
        this.loading = false;
      }
    });
  }

  // Filter fixtures for the selected week and group them by Date
  filterAndGroupFixtures() {
    const week = this.matchweeks[this.currentWeekIndex];
    const start = new Date(week.from).setHours(0,0,0,0);
    const end = new Date(week.to).setHours(23,59,59,999);

    // 1. Filter fixtures in range
    const weeksFixtures = this.allFixtures.filter(f => {
      const matchTime = new Date(f.matchDate).getTime();
      return matchTime >= start && matchTime <= end;
    });

    // 2. Sort by Date/Time
    weeksFixtures.sort((a, b) => 
      new Date(a.matchDate).getTime() - new Date(b.matchDate).getTime()
    );

    // 3. Group by Date Key (e.g., "Saturday 31 Jan")
    const groups = new Map<string, any[]>();

    weeksFixtures.forEach(f => {
      const dateKey = this.formatDateHeader(f.matchDate);
      if (!groups.has(dateKey)) {
        groups.set(dateKey, []);
      }
      groups.get(dateKey)?.push(f);
    });

    // Convert Map to Array for the HTML
    this.groupedFixtures = Array.from(groups, ([date, fixtures]) => ({ date, fixtures }));
  }

  formatDateHeader(dateStr: string): string {
    const date = new Date(dateStr);
    const today = new Date();
    today.setHours(0,0,0,0);
    
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);

    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);

    // Check for Today/Tomorrow/Yesterday
    if (date.getTime() === today.getTime()) return 'Today';
    if (date.getTime() === tomorrow.getTime()) return 'Tomorrow';
    if (date.getTime() === yesterday.getTime()) return 'Yesterday';

    // Otherwise standard format "Sat 31 Jan"
    return new Intl.DateTimeFormat('en-GB', { weekday: 'short', day: 'numeric', month: 'short' }).format(date);
  }

  previousWeek() {
    if (this.currentWeekIndex > 0) {
      this.currentWeekIndex--;
      this.filterAndGroupFixtures();
    }
  }

  nextWeek() {
    if (this.currentWeekIndex < this.matchweeks.length - 1) {
      this.currentWeekIndex++;
      this.filterAndGroupFixtures();
    }
  }
}
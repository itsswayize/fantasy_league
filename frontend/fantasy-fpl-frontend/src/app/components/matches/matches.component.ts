import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LeagueService } from '../../services/league.service';

@Component({
  selector: 'app-matches',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './matches.component.html',
  styleUrls: ['./matches.component.css']
})
export class MatchesComponent implements OnInit {
  groupedFixtures: any[] = [];
  loading: boolean = false;

  // Dynamic Week State
  currentWeekNumber: number = 1;
  viewStart!: Date;
  viewEnd!: Date;

  // Configuration: The official start of the 2025/2026 season
  private readonly SEASON_START_DATE = new Date('2025-08-15'); 

  constructor(private leagueService: LeagueService) {}

  ngOnInit(): void {
    this.initializeCurrentWeek();
    this.fetchDataForCurrentView();
  }

  // 1. Calculate which Game Week we are in today
  initializeCurrentWeek() {
    const today = new Date();
    
    // Calculate difference in milliseconds
    const diffTime = Math.abs(today.getTime() - this.SEASON_START_DATE.getTime());
    // Convert to days
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)); 
    
    // Calculate Week Number (approximate, assuming 1 week = 1 gameweek)
    this.currentWeekNumber = Math.ceil(diffDays / 7);
    
    // Safety check: Ensure we don't go below 1 or wildly above 38
    if (this.currentWeekNumber < 1) this.currentWeekNumber = 1;
    if (this.currentWeekNumber > 38) this.currentWeekNumber = 38;

    this.calculateDatesForWeek(this.currentWeekNumber);
  }

  // 2. Determine start/end dates based on Week Number
  calculateDatesForWeek(weekNum: number) {
    // Start date = SeasonStart + ((Week - 1) * 7 days)
    const daysToAdd = (weekNum - 1) * 7;
    
    const start = new Date(this.SEASON_START_DATE);
    start.setDate(start.getDate() + daysToAdd);
    
    const end = new Date(start);
    end.setDate(end.getDate() + 6); // A gameweek is roughly 7 days long (Fri-Thu)

    this.viewStart = start;
    this.viewEnd = end;
  }

  // 3. Fetch data from Backend using the calculated dates
  fetchDataForCurrentView() {
    this.loading = true;
    const fromStr = this.formatDateForApi(this.viewStart);
    const toStr = this.formatDateForApi(this.viewEnd);

    this.leagueService.getFixtures(fromStr, toStr).subscribe({
      next: (data) => {
        this.processFixtures(data);
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  // 4. Group by Date Header (e.g., "Sat 31 Jan")
  processFixtures(data: any[]) {
    // Sort by Date/Time first
    data.sort((a, b) => 
      new Date(a.matchDate + 'T' + (a.matchTime || '00:00')).getTime() - 
      new Date(b.matchDate + 'T' + (b.matchTime || '00:00')).getTime()
    );

    const groups = new Map<string, any[]>();

    data.forEach(f => {
      const header = this.formatDateHeader(f.matchDate);
      if (!groups.has(header)) {
        groups.set(header, []);
      }
      groups.get(header)?.push(f);
    });

    this.groupedFixtures = Array.from(groups, ([date, fixtures]) => ({ date, fixtures }));
  }

  // Navigation: Previous Week
  previousWeek() {
    if (this.currentWeekNumber > 1) {
      this.currentWeekNumber--;
      this.calculateDatesForWeek(this.currentWeekNumber);
      this.fetchDataForCurrentView();
    }
  }

  // Navigation: Next Week
  nextWeek() {
    // Allow going up to Week 38+ if needed
    this.currentWeekNumber++;
    this.calculateDatesForWeek(this.currentWeekNumber);
    this.fetchDataForCurrentView();
  }

  // Helpers
  private formatDateForApi(date: Date): string {
    return date.toISOString().split('T')[0]; // Returns YYYY-MM-DD
  }

  private formatDateHeader(dateStr: string): string {
    const d = new Date(dateStr);
    return new Intl.DateTimeFormat('en-GB', { weekday: 'short', day: 'numeric', month: 'short' }).format(d);
  }
}
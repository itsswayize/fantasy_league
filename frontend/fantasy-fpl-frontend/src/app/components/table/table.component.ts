import { Component, OnInit, OnDestroy } from '@angular/core';
import { interval, Subject } from 'rxjs';
import { takeUntil, startWith, switchMap } from 'rxjs/operators';
import { LeagueService } from '../../services/league.service';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.css']
})
export class TableComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  teams: any[] = [];
  isLoading: boolean = true; // Add a loading state

  constructor(private leagueService: LeagueService) {}

  ngOnInit(): void {
    // This setup calls the API immediately (0) and then every 60 seconds
    interval(60000)
      .pipe(
        startWith(0), 
        switchMap(() => this.leagueService.getStandings()),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (data) => {
          // Sort teams by points (highest first)
          this.teams = data.sort((a, b) => b.points - a.points);
          this.isLoading = false;
          console.log('Standings updated:', this.teams);
        },
        error: (err) => {
          console.error('Error fetching standings:', err);
          this.isLoading = false;
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
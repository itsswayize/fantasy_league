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
  standings: any[] = []; // Changed from 'teams' to match HTML
  isLoading: boolean = true;

  constructor(private leagueService: LeagueService) {}

  ngOnInit(): void {
    interval(60000)
      .pipe(
        startWith(0), 
        switchMap(() => this.leagueService.getStandings()),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (data) => {
          // Map and sort the data to ensure property names are consistent
          this.standings = data.sort((a, b) => b.points - a.points);
          this.isLoading = false;
          console.log('Standings updated:', this.standings);
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
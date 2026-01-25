import { Component, OnInit, OnDestroy } from '@angular/core';
import { interval, Subject } from 'rxjs';
import { takeUntil, startWith, switchMap } from 'rxjs/operators';
import { LeagueService } from '../../services/league.service'; //

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.css']
})
export class TableComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>(); //
  teams: any[] = [];

  constructor(private leagueService: LeagueService) {}

  ngOnInit(): void {
    // interval(60000) starts after 1 min; pipe it to refresh immediately on load
    interval(60000)
      .pipe(
        startWith(0), // Calls loadStandings immediately upon entry
        switchMap(() => this.leagueService.getStandings()), //
        takeUntil(this.destroy$) // Automatically cleans up when leaving the tab
      )
      .subscribe(data => {
        this.teams = data;
        // The backend sync ensures Man Utd's win is now reflected here
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next(); // Signals the observable to stop
    this.destroy$.complete(); //
  }
}
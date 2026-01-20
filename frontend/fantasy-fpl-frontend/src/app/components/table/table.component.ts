import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LeagueService } from '../../services/league.service';

@Component({
  selector: 'app-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './table.component.html',
  styleUrl: './table.component.css'
})
export class TableComponent implements OnInit {
  standings: any[] = [];

  constructor(private leagueService: LeagueService) {}

  ngOnInit(): void {
    // Automatically triggers sync-standings (Real API) then fetches local data
    this.leagueService.getStandings().subscribe({
      next: (data: any[]) => {
        this.standings = data;
      },
      error: (err: any) => console.error('Failed to sync standings', err)
    });
  }
}
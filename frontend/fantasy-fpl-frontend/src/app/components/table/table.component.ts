import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; // <--- CRITICAL for *ngFor
import { LeagueService } from '../../services/league.service';

@Component({
  selector: 'app-table',
  standalone: true,
  imports: [CommonModule], // <--- MUST INCLUDE THIS
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.css']
})
export class TableComponent implements OnInit {
  teams: any[] = [];

  constructor(private leagueService: LeagueService) {}

  ngOnInit(): void {
    this.fetchStandings();
  }

  fetchStandings() {
    this.leagueService.getStandings().subscribe({
      next: (data) => {
        console.log('API Data Received:', data);
        // Replace entire array instead of appending to prevent duplicates
        this.teams = data.sort((a: any, b: any) => 
          b.points - a.points || b.goalDifference - a.goalDifference
        );
      },
      error: (err) => console.error('Error fetching table:', err)
    });
  }

  // TrackBy function for better *ngFor performance and to prevent duplicate rendering
  trackByTeamId(index: number, team: any): any {
    return team.id || index;
  }
}
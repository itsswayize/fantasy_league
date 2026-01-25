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
  teams: any[] = []; // This variable name must match *ngFor="let team of teams"

  constructor(private leagueService: LeagueService) {}

  ngOnInit(): void {
    this.fetchStandings();
  }

  fetchStandings() {
    this.leagueService.getStandings().subscribe({
      next: (data) => {
        console.log('API Data Received:', data); // Check your browser console
        // Sort by points (highest first), then goal difference
        this.teams = data.sort((a: any, b: any) => 
          b.points - a.points || b.goalDifference - a.goalDifference
        );
      },
      error: (err) => console.error('Error fetching table:', err)
    });
  }
}
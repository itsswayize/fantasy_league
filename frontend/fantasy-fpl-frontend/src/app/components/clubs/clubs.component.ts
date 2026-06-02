import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router'; // For [routerLink]
import { LeagueService } from '../../services/league.service';

@Component({
  selector: 'app-clubs',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './clubs.component.html',
  styleUrl: './clubs.component.css'
})
export class ClubsComponent implements OnInit {
  clubs: any[] = [];

  constructor(private leagueService: LeagueService) {}

  ngOnInit(): void {
    this.fetchClubs();
  }

  fetchClubs(): void {
    this.leagueService.getStandings().subscribe({
      next: (data) => {
        // Replace entire array to prevent duplicates from accumulating
        this.clubs = data.sort((a, b) => a.name.localeCompare(b.name));
      },
      error: (err) => console.error('Error fetching clubs:', err)
    });
  }

  // TrackBy function for better *ngFor performance and to prevent duplicate rendering
  trackByClubId(index: number, club: any): any {
    return club.id || index;
  }
}
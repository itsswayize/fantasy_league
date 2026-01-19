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
    // getStandings returns the alphabetical list from the backend
    this.leagueService.getStandings().subscribe(data => {
      this.clubs = data.sort((a, b) => a.name.localeCompare(b.name));
    });
  }
}
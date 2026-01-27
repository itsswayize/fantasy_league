import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LeagueService } from '../../services/league.service';

@Component({
  selector: 'app-injuries',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './injuries.component.html',
  styleUrls: ['./injuries.component.css']
})
export class InjuriesComponent implements OnInit {
  // Use specific types or 'any' to fix the error
  injuredPlayers: any[] = [];
  loading: boolean = true;

  constructor(private leagueService: LeagueService) {}

  ngOnInit(): void {
    // Using the sync method we created earlier to ensure data is fresh
    this.leagueService.getClubsWithSync().subscribe({
      next: (teams: any[]) => { // Explicitly type 'teams' as any[]
        this.processInjuries(teams);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading injuries:', err);
        this.loading = false;
      }
    });
  }

  // Explicitly type the parameter here to fix the "implicit any" error
  processInjuries(teams: any[]) { 
    const injuries: any[] = [];

    teams.forEach((team: any) => {
      if (team.squad) {
        team.squad.forEach((player: any) => {
          if (player.injured) {
            injuries.push({
              ...player,
              clubName: team.name,
              clubLogo: team.logoUrl
            });
          }
        });
      }
    });

    // Sort alphabetically by club name
    this.injuredPlayers = injuries.sort((a, b) => a.clubName.localeCompare(b.clubName));
  }
}
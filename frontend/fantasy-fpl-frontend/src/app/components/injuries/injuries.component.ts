import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LeagueService } from '../../services/league.service';

@Component({
  selector: 'app-injuries',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './injuries.component.html',
  styleUrl: './injuries.component.css'
})
export class InjuriesComponent implements OnInit {
  groupedInjuries: { clubName: string, players: any[] }[] = [];

  constructor(private leagueService: LeagueService) {}

  // injuries.component.ts
ngOnInit(): void {
  // Chain: Sync Injuries first, then Load them
  this.leagueService.syncInjuries().subscribe(() => {
    this.loadInjuries();
  });
}
  loadInjuries(): void {
  // Use getStandings to get teams and their nested squads
  this.leagueService.getStandings().subscribe(teams => {
    const groups: { [key: string]: any[] } = {};

    teams.forEach(team => {
      // Filter for players marked 'injured' by the API sync
      const injuredInTeam = team.squad?.filter((p: any) => p.injured) || [];
      
      if (injuredInTeam.length > 0) {
        injuredInTeam.sort((a: any, b: any) => a.name.localeCompare(b.name));
        groups[team.name] = injuredInTeam;
      }
    });

    this.groupedInjuries = Object.keys(groups)
      .sort((a, b) => a.localeCompare(b))
      .map(clubName => ({
        clubName: clubName,
        players: groups[clubName]
      }));
  });
}
}
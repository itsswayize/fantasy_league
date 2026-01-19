import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LeagueService } from '../../services/league.service';

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './statistics.component.html',
  styleUrl: './statistics.component.css'
})
export class StatisticsComponent implements OnInit {
  topScorers: any[] = [];
  topAssists: any[] = [];

  constructor(private leagueService: LeagueService) {}

  ngOnInit(): void {
    this.leagueService.getStatisticsWithSync().subscribe(teams => {
      let allPlayers: any[] = [];
      teams.forEach(team => {
        if (team.squad) {
          allPlayers = [...allPlayers, ...team.squad.map((p: any) => ({...p, teamName: team.name}))];
        }
      });

      // Filter and Sort Top 10 Scorers
      this.topScorers = allPlayers
        .filter(p => p.goals > 0)
        .sort((a, b) => b.goals - a.goals)
        .slice(0, 10);

      // Filter and Sort Top 10 Assists
      this.topAssists = allPlayers
        .filter(p => p.assists > 0)
        .sort((a, b) => b.assists - a.assists)
        .slice(0, 10);
    });
}
}
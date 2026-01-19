import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { LeagueService } from '../../services/league.service';

@Component({
  selector: 'app-club-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './club-detail.component.html',
  styleUrl: './club-detail.component.css'
})
export class ClubDetailComponent implements OnInit {
  club: any = null;
  fixtures: any[] = [];
  activeTab: string = 'squad'; // Default tab

  constructor(
    private route: ActivatedRoute,
    private leagueService: LeagueService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      
      // Fetch Club Details (including squad)
      this.leagueService.getClubDetails(id).subscribe({
        next: (data: any) => {
          this.club = data;
        },
        error: (err: any) => console.error('Error fetching club details', err)
      });

      // Fetch Club-Specific Fixtures
      this.leagueService.getClubFixtures(id).subscribe({
        next: (data: any[]) => {
          this.fixtures = data;
        },
        error: (err: any) => console.error('Error fetching club fixtures', err)
      });
    }
  }

  setTab(tabName: string): void {
    this.activeTab = tabName;
  }
}
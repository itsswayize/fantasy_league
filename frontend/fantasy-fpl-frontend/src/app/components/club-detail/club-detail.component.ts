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
  groupedFixtures: any[] = []; // Changed to store grouped fixtures
  activeTab: string = 'squad';

  constructor(
    private route: ActivatedRoute,
    private leagueService: LeagueService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      
      this.leagueService.getClubDetails(id).subscribe({
        next: (data: any) => {
          this.club = data;
        },
        error: (err: any) => console.error('Error fetching club details', err)
      });

      this.leagueService.getClubFixtures(id).subscribe({
        next: (data: any[]) => {
          // Remove duplicates
          const uniqueData = data.filter((value, index, self) =>
            index === self.findIndex((t) => (
              t.id === value.id
            ))
          );
          this.processFixtures(uniqueData);
        },
        error: (err: any) => console.error('Error fetching club fixtures', err)
      });
    }
  }

  setTab(tabName: string): void {
    this.activeTab = tabName;
  }

  processFixtures(data: any[]) {
    // Sort by Date
    data.sort((a, b) => 
      new Date(a.matchDate + 'T' + (a.matchTime || '00:00')).getTime() - 
      new Date(b.matchDate + 'T' + (b.matchTime || '00:00')).getTime()
    );

    const groups = new Map<string, any[]>();

    data.forEach(f => {
      const header = this.formatDateHeader(f.matchDate);
      if (!groups.has(header)) {
        groups.set(header, []);
      }
      groups.get(header)?.push(f);
    });

    this.groupedFixtures = Array.from(groups, ([date, fixtures]) => ({ date, fixtures }));
  }

  private formatDateHeader(dateStr: string): string {
    const d = new Date(dateStr);
    return new Intl.DateTimeFormat('en-GB', { weekday: 'short', day: 'numeric', month: 'short' }).format(d);
  }
}
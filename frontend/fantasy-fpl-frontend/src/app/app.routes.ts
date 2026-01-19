import { Routes } from '@angular/router';
import { MatchesComponent } from './components/matches/matches.component';
import { TableComponent } from './components/table/table.component';
import { InjuriesComponent } from './components/injuries/injuries.component';
import { ClubsComponent } from './components/clubs/clubs.component';
import { ClubDetailComponent } from './components/club-detail/club-detail.component';

export const routes: Routes = [
  { path: '', redirectTo: '/matches', pathMatch: 'full' },
  { path: 'matches', component: MatchesComponent },
  { path: 'table', component: TableComponent },
  { path: 'injuries', component: InjuriesComponent },
  { path: 'clubs', component: ClubsComponent },
  { path: 'clubs/:id', component: ClubDetailComponent }, // Dynamic club page
];
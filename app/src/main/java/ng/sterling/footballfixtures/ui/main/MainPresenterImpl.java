package ng.sterling.footballfixtures.ui.main;


/**
 * Author - Oluwatobi Adenekan
 * Date   - 12/08/2019
 **/


import android.app.Application;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;
import ng.sterling.footballfixtures.FootBallFixture;
import ng.sterling.footballfixtures.R;
import ng.sterling.footballfixtures.dto.response.CompetitionResponse;
import ng.sterling.footballfixtures.dto.response.MainResponseDto;
import ng.sterling.footballfixtures.dto.response.MatchResponse;
import ng.sterling.footballfixtures.model.ApiSuccessResponse;
import ng.sterling.footballfixtures.network.ApiCallBack;
import ng.sterling.footballfixtures.network.ApiClient;
import ng.sterling.footballfixtures.ui.main.adapters.MainFragmentAdapter;


/**
 * Author: Oluwatobi Adenekan
 * date:    09/08/2019
 **/

public class MainPresenterImpl implements MainPresenter {

    private final String TAG = MainPresenterImpl.class.getSimpleName();
    public ApiClient apiClient;

    private MainView mainView;

    private FragmentManager fragmentManager;

    EventBus eventBus;

    @Inject
    Context context;


    @Inject
    public MainPresenterImpl(MainView mainView, EventBus eventBus, ApiClient apiClient) {
        this.mainView = mainView;
        this.eventBus = eventBus;
        this.apiClient = apiClient;
        FootBallFixture.getAppComponent().inject(this);
    }

    private Observable<MatchResponse> fetchMatches() {
        return apiClient.getApiService()
                .getMatches()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<CompetitionResponse> fetchCompetitions() {
        return apiClient.getApiService().getCompetions().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<MainResponseDto> fetchMatchesAndCompetitions() {
        return Observable.zip(fetchMatches(), fetchCompetitions(), new BiFunction<MatchResponse, CompetitionResponse, MainResponseDto>() {
            @Override
            public MainResponseDto apply(MatchResponse matchResponse, CompetitionResponse competitionResponse) throws Exception {
                return new MainResponseDto(matchResponse, competitionResponse);
            }
        });
    }

    public void getMatchesAndCompetitions() {
        fetchMatchesAndCompetitions().subscribe(new ApiCallBack<MainResponseDto>());
    }



    /**
     * This method will be called by the event bus once data is available.
     *
     * @param response
     */
    @Subscribe
    public void onGetMatchAndCompetion(ApiSuccessResponse<MainResponseDto> response) {

        Log.e(TAG, "onGetMatchAndCompetion: " + "populated the adapter" );
        mainView.setResponse(response.getData());


    }


    @Override
    public void onPause() {

        unRegisterFromEventBus();

    }

    @Override
    public void onResume() {

        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }

    @Override
    public void onDestroy() {

        unRegisterFromEventBus();

    }

    private void unRegisterFromEventBus() {
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
        }
    }
}
